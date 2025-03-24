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
        startServer();
    }

    @Override
    public void eventHandlers() {
        FileChooser fileChooser = new FileChooser();
        selectFileBtn.setOnAction(e -> {
            try {
                selectedFile = fileChooser.showOpenDialog(stage);
                if (selectedFile != null) {
                    statusArea.appendText("Selected: " + selectedFile.getName() + "\n");
                    sendBtn.setDisable(false);
                }
            } catch (Exception ex) {
                statusArea.appendText("Error occurred when trying to choose files!\n");
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
            try (ServerSocket messageServerSocket = new ServerSocket(5050);
                 Socket clientSocket = messageServerSocket.accept();
                 OutputStream output = clientSocket.getOutputStream();
                 PrintWriter writer = new PrintWriter(output, true)) {

                Des des = new Des();
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
                byte[] encryptedBinary = des.encrypt(messageBytes, keyBytes, false);
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

    //Check ketnoi
    private void startServer() {
        new Thread(() -> {
            try (ServerSocket messageServerSocket = new ServerSocket(5050)) {
                Platform.runLater(() -> statusArea.appendText("Server is running on port 5050...\n"));

                Socket clientSocket = messageServerSocket.accept();
                Platform.runLater(() -> statusArea.appendText("Client connected!\n"));
                handleClient(clientSocket);

            } catch (Exception e) {
                Platform.runLater(() -> statusArea.appendText("Error: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try (OutputStream output = clientSocket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            String message = messageInput.getText();
            String encryptionKey = encryptionKeyInput.getText();

            if (message.isEmpty() || encryptionKey.isEmpty()) {
                Platform.runLater(() -> statusArea.appendText("Please enter both message and encryption key!\n"));
                return;
            }

            Des des = new Des();
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBinary = des.encrypt(messageBytes, keyBytes, false); // Thêm 'false' hoặc 'true'

            String hexMessage = bytesToHex(encryptedBinary);
            writer.println(hexMessage);

            Platform.runLater(() -> statusArea.appendText("Encrypted message sent!\n"));
        } catch (Exception e) {
            Platform.runLater(() -> statusArea.appendText("Error sending encrypted message: " + e.getMessage() + "\n"));
        }
    }


}
