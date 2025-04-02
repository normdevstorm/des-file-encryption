package com.normdevstorm.encryptedfiletransfer.server.controller;
import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import com.normdevstorm.encryptedfiletransfer.utils.constant.ConstantManager;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.normdevstorm.encryptedfiletransfer.crypto.Des.byteArrayToHexString;


public class ServerController extends GenericUIController {

    @FXML
    private Button sendBtn;
    @FXML private Button sendTextBtn;
    @FXML private TextArea messageInput;
    @FXML private TextField encryptionKeyInput;
    private File selectedFile;
    private ServerSocket serverSocket;
    private Stage stage;
    private final List<Socket> connectedClients = new ArrayList<>();


    public void initializeController(ServerSocket serverSocket, Stage stage) {
        this.serverSocket = serverSocket;
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
                    statusArea.appendText("Error occurred when trying to choosing files !!!");
                }
            });

            sendBtn.setOnAction(e -> {
                try {
                    sendFile(serverSocket);
                } catch (Exception ex) {
                    statusArea.appendText("Error sending file: " + selectedFile.getName() + ex.getMessage());
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

    private synchronized void sendFile(ServerSocket serverSocket) {
        if (selectedFile != null) {
            Platform.runLater(this::notifyClientToSendFiles
            );
        } else {
            Platform.runLater(this::showAlertNoFileChosenDialog
            );
        }
    }

    private void showAlertNoFileChosenDialog(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Send File Warning");
        alert.setHeaderText("No File Selected");
        alert.setContentText("Please select a file before sending.");
        alert.showAndWait();
        statusArea.appendText("No file selected for sending\n");
    }

    private void notifyClientToSendFiles(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Send File");
        alert.setHeaderText("Send " + selectedFile.getName());
        alert.setContentText("Do you want to send this file to the client?");

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    // Signal the client about pending file transfer using signaling port
                    Socket signalSocket = new Socket(ConstantManager.clientIpAddress, ConstantManager.SIGNALING_PORT);
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(signalSocket.getOutputStream()));
                    BufferedReader in = new BufferedReader(new InputStreamReader(signalSocket.getInputStream()));

                    // Send handshake signal
                    out.write("Start handshake protocol\n");
                    out.flush();
                    statusArea.appendText("Sent file transfer request to client\n");

                    // Wait for response
                    String response = in.readLine();
                    if (response != null && response.equals("Yes")) {
                        statusArea.appendText("Client accepted the file transfer\n");

                        // Prompt for encryption key
                        TextInputDialog keyDialog = new TextInputDialog();
                        keyDialog.setTitle("Encryption Key");
                        keyDialog.setHeaderText("Enter encryption key");
                        keyDialog.setContentText("Key (8 characters):");

                        keyDialog.showAndWait().ifPresent(this::getKeyFromServer
                        );
                    } else {
                        statusArea.appendText("Client declined the file transfer\n");
                    }
                    signalSocket.close();
                } catch (IOException e) {
                    statusArea.appendText("Error initiating file transfer: " + e.getMessage() + "\n");
                }
            } else {
                statusArea.appendText("File transfer cancelled by server\n");
            }
        });
    }

    private void getKeyFromServer(String key){
        if (key.length() < 8) {
            statusArea.appendText("Key must be at least 8 characters long\n");
            return;
        }

        statusArea.appendText("Key accepted. Starting file transfer...\n");

        // Start the actual file transfer on the original port
        SendFileThread sendFileThread = new SendFileThread(statusArea, serverSocket, key);
        sendFileThread.setSelectedFile(selectedFile);
        sendFileThread.setType(FileType.IMAGE);

        // Pass the encryption key to the thread
        try {
            sendFileThread.start();
        } catch (Exception e) {
            statusArea.appendText("Error starting file transfer: " + e.getMessage() + "\n");
        }
    }
    private void startServer() {
        new Thread(() -> {
            try {
                try (ServerSocket messageServerSocket = new ServerSocket(ConstantManager.MESSAGING_PORT)) {
                    Platform.runLater(() -> statusArea.appendText("Server is running on port 5050...\n"));

                    while (true) {
                        Socket clientSocket = messageServerSocket.accept();
                        connectedClients.add(clientSocket); // Thêm client vào danh sách
                        Platform.runLater(() -> statusArea.appendText("Client connected!\n"));
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> statusArea.appendText("Error: " + e.getMessage() + "\n"));
            }
        }).start();
    }


    private void sendEncryptedMessage(String message, String encryptionKey) {
        new Thread(() ->
        {
            try {
                if (message.isEmpty() || encryptionKey.isEmpty()) {
                    Platform.runLater(() -> statusArea.appendText("Please enter both message and encryption key!\n"));
                    return;
                }

                // Đảm bảo khóa có độ dài chính xác cho DES (8 byte)
                byte[] keyBytes = encryptionKey.getBytes();
                if (keyBytes.length < 8) {
                    Platform.runLater(() -> statusArea.appendText("Error: Encryption key must be at least 8 bytes!\n"));
                    return;
                }

                Des des = new Des();
                byte[] messageBytes = message.getBytes();
                byte[] encryptedByte = des.encrypt(messageBytes, keyBytes, false);

                // Gửi tin nhắn đến tất cả client đã kết nối
                for (Socket clientSocket : connectedClients) {
                    if (!clientSocket.isClosed()) { // Kiểm tra kết nối
                        OutputStream output = clientSocket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);
                        writer.println(Des.byteArrayToHexString(encryptedByte));
                    }
                }

                Platform.runLater(() -> statusArea.appendText("Encrypted message sent!\n"));
            } catch (Exception e) {
                Platform.runLater(() -> statusArea.appendText("Error sending encrypted message: " + e.getMessage() + "\n"));
            }
        }).start();
    }

}

