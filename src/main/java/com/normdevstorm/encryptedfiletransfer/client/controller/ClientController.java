//package com.normdevstorm.encryptedfiletransfer.client.controller;
//
//import com.normdevstorm.encryptedfiletransfer.crypto.Des;
//import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
//import com.normdevstorm.encryptedfiletransfer.model.KeyModel;
//import com.normdevstorm.encryptedfiletransfer.utils.constant.ConstantManager;
//import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
//import com.normdevstorm.encryptedfiletransfer.utils.threads.ReceiveFileThread;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import java.io.*;
//import java.net.Socket;
//
//import javafx.scene.control.Button;
//import javafx.scene.control.TextArea;
//import javafx.stage.Stage;
//
//
//public class ClientController extends GenericUIController {
//
//    @FXML private Button receiveBtn;
//    @FXML private Button handShakeRequest;
//    @FXML private TextArea statusArea;
//    static private Socket clientSocket;
//    static private KeyModel keyModel;
//
//    private Stage stage;
//
//    public void initializeController() {
//        try {
//            keyModel = new KeyModel();
//            clientSocket = new Socket(ConstantManager.serverIpAddress, 80);
//             eventHandlers();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    @Override
//    public void eventHandlers() {
//        receiveBtn.setOnAction(actionEvent -> {
//                    receiveFile(FileType.IMAGE, statusArea);
//                }
//        );
//    }
//
//    private synchronized void receiveFile(FileType type, TextArea statusArea) {
//            Des des = new Des();
//            ReceiveFileThread receiveFileThread = new ReceiveFileThread(FileType.IMAGE, statusArea, des, clientSocket);
//            Platform.runLater(() -> {
//                receiveFileThread.start();
//            });
//    }
//}
//?? tren la code cu nhe


package com.normdevstorm.encryptedfiletransfer.client.controller;

//import com.normdevstorm.encryptedfiletransfer.crypto.DES;
//import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import com.normdevstorm.encryptedfiletransfer.model.KeyModel;
import com.normdevstorm.encryptedfiletransfer.utils.constant.ConstantManager;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import com.normdevstorm.encryptedfiletransfer.utils.threads.ReceiveFileThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientController extends GenericUIController {

    @FXML private Button receiveBtn;
    @FXML private Button receiveMsgBtn;
    @FXML private Button decryptBtn;
    @FXML private TextArea statusArea;
    @FXML private TextField decryptionKeyInput;

    //@FXML private Button receiveMsgBtn; // Đảm bảo khai báo đúng biến này


    private Socket clientSocket;
    private KeyModel keyModel;
    private String lastEncryptedMessage;
    private Stage stage;

    public void initializeController() {
        try {
            keyModel = new KeyModel();
            clientSocket = new Socket(ConstantManager.serverIpAddress, 80);
            eventHandlers();
        } catch (Exception e) {
            Platform.runLater(() -> statusArea.appendText("Error connecting to server: " + e.getMessage() + "\n"));
        }
    }

    @Override
    public void eventHandlers() {
        receiveBtn.setOnAction(actionEvent -> receiveFile(FileType.IMAGE));

        receiveMsgBtn.setOnAction(actionEvent -> receiveMessage());
        decryptBtn.setOnAction(actionEvent -> decryptMessage());
        receiveMsgBtn.setOnAction(actionEvent -> receiveMessage());

    }

    private synchronized void receiveFile(FileType type) {
        Des des = new Des();
        ReceiveFileThread receiveFileThread = new ReceiveFileThread(type, statusArea, des, clientSocket);
        receiveFileThread.start();
    }

    private void receiveMessage() {
        new Thread(() -> {
            try (Socket msgSocket = new Socket("localhost", 5000);
                 BufferedReader in = new BufferedReader(new InputStreamReader(msgSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(msgSocket.getOutputStream(), true)) {

                lastEncryptedMessage = in.readLine();
                if (lastEncryptedMessage != null) {
                    Platform.runLater(() -> statusArea.appendText("Encrypted message received (HEX): " + lastEncryptedMessage + "\n"));
                }

            } catch (Exception e) {
                Platform.runLater(() -> statusArea.appendText("Error receiving message: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    private void decryptMessage() {
        if (lastEncryptedMessage == null || lastEncryptedMessage.isEmpty()) {
            Platform.runLater(() -> statusArea.appendText("No encrypted message to decrypt!\n"));
            return;
        }

        String providedKey = decryptionKeyInput.getText();
        if (providedKey == null || providedKey.isEmpty()) {
            Platform.runLater(() -> statusArea.appendText("Please enter a decryption key!\n"));
            return;
        }

        try {
            Des des = new Des();

            // Chuyển đổi từ HEX -> byte[]
            byte[] binaryMessage = hexStringToByteArray(lastEncryptedMessage);

            // Chuyển đổi key từ String sang byte[]
            byte[] keyBytes = providedKey.getBytes("UTF-8");

            // Giải mã
            byte[] decryptedBinary = des.decrypt(keyBytes, binaryMessage);

            // Chuyển đổi từ binary sang String UTF-8
            String decryptedMessage = new String(decryptedBinary, "UTF-8");

            Platform.runLater(() -> statusArea.appendText("Decrypted message: " + decryptedMessage + "\n"));
        } catch (Exception e) {
            Platform.runLater(() -> statusArea.appendText("Decryption failed: Incorrect key or invalid data!\n"));
        }
    }

    // Hàm chuyển đổi HEX -> byte[]
    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }


}
