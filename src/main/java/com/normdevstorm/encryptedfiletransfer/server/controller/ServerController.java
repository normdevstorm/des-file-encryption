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
import java.util.List;
import java.util.ArrayList;


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

    private final List<Socket> connectedClients = new ArrayList<>();

    private void startServer() {
        new Thread(() -> {
            try {
                ServerSocket messageServerSocket = new ServerSocket(5050);
                Platform.runLater(() -> statusArea.appendText("Server is running on port 5050...\n"));

                while (true) {
                    Socket clientSocket = messageServerSocket.accept();
                    connectedClients.add(clientSocket); // Thêm client vào danh sách
                    Platform.runLater(() -> statusArea.appendText("Client connected!\n"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> statusArea.appendText("Error: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    private void sendEncryptedMessage(String message, String encryptionKey) {
        new Thread(() -> {
            try {
                if (message.isEmpty() || encryptionKey.isEmpty()) {
                    Platform.runLater(() -> statusArea.appendText("Please enter both message and encryption key!\n"));
                    return;
                }

                // Đảm bảo khóa có độ dài chính xác cho DES (8 byte)
                byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
                if (keyBytes.length < 8) {
                    Platform.runLater(() -> statusArea.appendText("Error: Encryption key must be at least 8 bytes!\n"));
                    return;
                }

                Des des = new Des();
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                byte[] encryptedBinary = des.encrypt(messageBytes, keyBytes, false);
                String hexMessage = bytesToHex(encryptedBinary);

                // Gửi tin nhắn đến tất cả client đã kết nối
                for (Socket clientSocket : connectedClients) {
                    if (!clientSocket.isClosed()) { // Kiểm tra kết nối
                        OutputStream output = clientSocket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);
                        writer.println(hexMessage);
                    }
                }

                Platform.runLater(() -> statusArea.appendText("Encrypted message sent!\n"));
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



    private void handleClient(Socket clientSocket) {
        try {
            OutputStream output = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            while (true) { // Giữ kết nối và gửi nhiều tin nhắn
                String message = messageInput.getText();
                String encryptionKey = encryptionKeyInput.getText();

                if (!message.isEmpty() && !encryptionKey.isEmpty()) {
                    Des des = new Des();
                    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                    byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
                    byte[] encryptedBinary = des.encrypt(messageBytes, keyBytes, false);

                    String hexMessage = bytesToHex(encryptedBinary);
                    writer.println(hexMessage);

                    Platform.runLater(() -> statusArea.appendText("Encrypted message sent!\n"));
                }

                Thread.sleep(5000); // Gửi tin nhắn sau mỗi 5 giây (hoặc tùy chỉnh)
            }

        } catch (Exception e) {
            Platform.runLater(() -> statusArea.appendText("Client disconnected.\n"));
        }
    }

}
