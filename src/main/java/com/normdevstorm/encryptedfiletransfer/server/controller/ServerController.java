//package com.normdevstorm.encryptedfiletransfer.server.controller;
//import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
//import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
//import com.normdevstorm.encryptedfiletransfer.utils.threads.SendFileThread;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//
//import java.io.*;
//import java.net.ServerSocket;
//
//
//public class ServerController extends GenericUIController {
//
//    @FXML
//    private Button sendBtn;
////    @FXML private Button receiveBtn;
//
//    private File selectedFile;
//    private ServerSocket serverSocket;
//    private Stage stage;
//
//
//    public void initializeController(ServerSocket serverSocket, Stage stage) {
//        this.serverSocket = serverSocket;
//        this.stage = stage;
//        eventHandlers();
//    }
//
//
//    @Override
//    public void eventHandlers() {
//            FileChooser fileChooser = new FileChooser();
//            selectFileBtn.setOnAction(e -> {
//                try {
//                    selectedFile = fileChooser.showOpenDialog(stage);
//                    if (selectedFile != null) {
//                        statusArea.appendText("Selected: " + selectedFile.getName() + "\n");
//                        sendBtn.setDisable(false);
//                    }
//                } catch (Exception ex) {
//                    statusArea.appendText("Error occurred when trying to choosing files !!!");
//                }
//            });
//
//            sendBtn.setOnAction(e -> {
//                try {
//                    sendFile(serverSocket);
//                } catch (Exception ex) {
//                    statusArea.appendText("Error sending file: " + selectedFile.getName() + ex.getMessage());
//                }
//            });
//    }
//
//    private synchronized void sendFile(ServerSocket serverSocket) {
//        if (selectedFile != null) {
//            SendFileThread sendFileThread = new SendFileThread(statusArea, serverSocket);
//            sendFileThread.setSelectedFile(selectedFile);
//            sendFileThread.setType(FileType.IMAGE);
//            Platform.runLater(sendFileThread::start);
//        } else {
//            /// TODO: add showDialog here
//            System.out.println("File is null !!!");
//        }
//    }
//}
//Tren la code cu nhe


package com.normdevstorm.encryptedfiletransfer.server.controller;

import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import com.normdevstorm.encryptedfiletransfer.utils.threads.SendFileThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerController extends GenericUIController {

    @FXML private Button sendBtn;
    @FXML private Button selectFileBtn;
    @FXML private Button sendTextBtn;
    @FXML private TextArea messageInput;
    @FXML private TextField encryptionKeyInput;
    @FXML private TextArea statusArea;


    private File selectedFile;
    private ServerSocket fileServerSocket;
    private Stage stage;

    public void initializeController(ServerSocket fileServerSocket, Stage stage) {
        this.fileServerSocket = fileServerSocket;
        this.stage = stage;
        eventHandlers();
    }

    @Override
    public void eventHandlers() {
        FileChooser fileChooser = new FileChooser();

        sendBtn.setOnAction(e -> {
            try {
                selectedFile = fileChooser.showOpenDialog(stage);
                if (selectedFile != null) {
                    statusArea.appendText("Selected file: " + selectedFile.getName() + "\n");
                    sendFile(selectedFile);
                }
            } catch (Exception ex) {
                statusArea.appendText("Error selecting file!\n");
            }
        });

        sendTextBtn.setOnAction(e -> {
            String message = messageInput.getText();
            String encryptionKey = encryptionKeyInput.getText();

            if (message.isEmpty() || encryptionKey.isEmpty()) {
                statusArea.appendText("Please enter both message and encryption key!\n");
                return;
            }

            sendEncryptedMessage(message, encryptionKey);
        });
    }

    private synchronized void sendFile(File file) {
        if (file != null) {
            SendFileThread sendFileThread = new SendFileThread(statusArea, fileServerSocket);
            sendFileThread.setSelectedFile(file);
            sendFileThread.setType(FileType.IMAGE);
            Platform.runLater(sendFileThread::start);
        } else {
            statusArea.appendText("No file selected!\n");
        }
    }

    private void sendEncryptedMessage(String message, String encryptionKey) {
        new Thread(() -> {
            try (ServerSocket messageServerSocket = new ServerSocket(5000);
                 Socket clientSocket = messageServerSocket.accept();
                 OutputStream output = clientSocket.getOutputStream();
                 PrintWriter writer = new PrintWriter(output, true)) {

                Des des = new Des();

                // Chuyển message sang byte[]
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

                // Chuyển key sang byte[]
                byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);

                // Mã hóa
                byte[] encryptedBinary = des.encrypt(messageBytes, keyBytes, false);

                // Chuyển sang HEX để gửi
                String hexMessage = bytesToHex(encryptedBinary);

                writer.println(hexMessage);
                Platform.runLater(() -> statusArea.appendText("Encrypted message (HEX) sent successfully!\n"));

            } catch (Exception e) {
                Platform.runLater(() -> statusArea.appendText("Error sending encrypted message: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
}
