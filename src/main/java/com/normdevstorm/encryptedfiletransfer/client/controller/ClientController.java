package com.normdevstorm.encryptedfiletransfer.client.controller;

import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ClientController {

    @FXML private TextArea receivedMessageArea;

    public void initializeController() {
        new Thread(this::receiveEncryptedMessage).start();
    }

    private void receiveEncryptedMessage() {
        boolean isConnected = false; // Biến kiểm tra trạng thái kết nối
        while (true) {
            try (Socket socket = new Socket("localhost", 5050);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                if (!isConnected) { // Chỉ in khi lần đầu kết nối thành công
                    isConnected = true;
                    Platform.runLater(() -> receivedMessageArea.appendText("Connected to server!\n"));
                }

                String encryptedHex = reader.readLine();

                if (encryptedHex != null) {
                    Platform.runLater(() -> showMessageDialog(encryptedHex));
                }
            } catch (Exception e) {
                if (isConnected) { // Chỉ in khi mất kết nối lần đầu
                    isConnected = false;
                    Platform.runLater(() -> receivedMessageArea.appendText("Waiting for server...\n"));
                }
                try {
                    Thread.sleep(2000); // Chờ 2 giây rồi thử lại
                } catch (InterruptedException ignored) {}
            }
        }
    }


    private void showMessageDialog(String encryptedHex) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Encrypted Message Received");
        alert.setHeaderText("You have received an encrypted message.");
        alert.setContentText("Do you want to decrypt it?");

        ButtonType decryptButton = new ButtonType("Decrypt");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(decryptButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == decryptButton) {
            requestDecryptionKey(encryptedHex);
        }
    }

    private void requestDecryptionKey(String encryptedHex) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Decryption Key");
        dialog.setHeaderText("Enter the 8-character decryption key:");
        dialog.setContentText("Key:");

        Optional<String> keyInput = dialog.showAndWait();
        keyInput.ifPresent(key -> {
            if (key.length() != 8) {
                receivedMessageArea.appendText("Invalid key! Must be 8 characters.\n");
            } else {
                decryptMessage(encryptedHex, key);
            }
        });
    }

    private void decryptMessage(String encryptedHex, String decryptionKey) {
        try {
            Des des = new Des();
            byte[] encryptedBytes = hexToBytes(encryptedHex);
            byte[] keyBytes = decryptionKey.getBytes(StandardCharsets.UTF_8);
            byte[] decryptedBytes = des.decrypt(encryptedBytes, keyBytes);

            String decryptedMessage = new String(decryptedBytes, StandardCharsets.UTF_8);
            Platform.runLater(() -> receivedMessageArea.appendText("Decrypted Message: " + decryptedMessage + "\n"));
        } catch (Exception e) {
            Platform.runLater(() -> receivedMessageArea.appendText("Decryption failed: " + e.getMessage() + "\n"));
        }
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
