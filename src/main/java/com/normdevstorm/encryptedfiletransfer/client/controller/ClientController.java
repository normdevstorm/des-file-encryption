package com.normdevstorm.encryptedfiletransfer.client.controller;

import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import com.normdevstorm.encryptedfiletransfer.model.KeyModel;
import com.normdevstorm.encryptedfiletransfer.utils.constant.ConstantManager;
import com.normdevstorm.encryptedfiletransfer.utils.threads.ClientListenerThread;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

import javafx.scene.control.*;
import javafx.stage.Stage;

import static com.normdevstorm.encryptedfiletransfer.crypto.Des.byteArrayToHexString;

public class ClientController extends GenericUIController {

    @FXML
    private Button handShakeRequest;
    @FXML
    private TextArea statusArea;
    @FXML
    private TextArea receivedMessageArea;
    static private Socket clientSocket;
    static private KeyModel keyModel;

    private Stage stage;

    public void initializeController() {
        try {
            keyModel = new KeyModel();
            clientSocket = new Socket(ConstantManager.serverIpAddress, ConstantManager.FILE_TRANSFER_PORT);
            eventHandlers();
            // Start the signal listener thread
            ClientListenerThread listenerThread = new ClientListenerThread(statusArea, clientSocket);
            listenerThread.setDaemon(true);
            listenerThread.start();
            statusArea.appendText("Connected to server and listening for file transfers\n");
            // Message thread
            new Thread(this::receiveEncryptedMessage).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void eventHandlers() {
    }

    private void receiveEncryptedMessage() {
        boolean isConnected = false; // Biến kiểm tra trạng thái kết nối
        while (true) {
            try (Socket socket = new Socket(ConstantManager.serverIpAddress, ConstantManager.MESSAGING_PORT); BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                if (!isConnected) { // Chỉ in khi lần đầu kết nối thành công
                    isConnected = true;
                    Platform.runLater(() -> receivedMessageArea.appendText("Connected to server!\n"));
                }

                String encryptedData;

                while ((encryptedData = reader.readLine()) != null) { // Nhận dữ liệu liên tục


                    String finalEncryptedHex = encryptedData;


                    Platform.runLater(() -> {


                        showMessageDialog(finalEncryptedHex);


                        receivedMessageArea.appendText("Waiting for server...\n");


                    });
                }
            } catch (Exception e) {
                if (isConnected) { // Chỉ in khi mất kết nối lần đầu
                    isConnected = false;
                    Platform.runLater(() -> receivedMessageArea.appendText("Waiting for server...\n"));
                }
                try {
                    Thread.sleep(2000); // Chờ 2 giây rồi thử lại
                } catch (InterruptedException ignored) {
                }
            }
        }
    }


    private void showMessageDialog(String encryptedData) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Encrypted Message Received");
        alert.setHeaderText("You have received an encrypted message.");
        alert.setContentText("Do you want to decrypt it?");

        ButtonType decryptButton = new ButtonType("Decrypt");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(decryptButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == decryptButton) {
            requestDecryptionKey(encryptedData);
        }
    }

    private void requestDecryptionKey(String encryptedData) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Decryption Key");
        dialog.setHeaderText("Enter the decryption key with the minimum length of 8 characters:");
        dialog.setContentText("Key:");

        Optional<String> keyInput = dialog.showAndWait();
        keyInput.ifPresent(key -> {
            if (key.trim().isEmpty()) { // Kiểm tra nếu để trống
                receivedMessageArea.appendText("Invalid key! Cannot be empty.\n");
            }
            if (key.length() < 8) {
                receivedMessageArea.appendText("Invalid key! Must be 8 characters or more.\n");
            } else {
                decryptMessage(encryptedData, key);
            }
        });
    }

    private void decryptMessage(String encryptedData, String decryptionKey) {
        try {
            Des des = new Des();
            System.out.println("Encrypted data: " + encryptedData);
            System.out.println("Key: " + decryptionKey);
            byte[] encryptedBytes = Des.hexStringToByteArray(encryptedData);
            byte[] keyBytes = decryptionKey.getBytes();
            byte[] decryptedBytes = des.encryptText(encryptedBytes, keyBytes, true);
            String decryptedMessage = new String(decryptedBytes);
            System.out.println(decryptedMessage);
            Platform.runLater(() -> receivedMessageArea.appendText("Decrypted Message: " + decryptedMessage + "\n"));
        } catch (Exception e) {
            Platform.runLater(() -> receivedMessageArea.appendText("Decryption failed: " + e.getMessage() + "\n"));
        }
    }
}
