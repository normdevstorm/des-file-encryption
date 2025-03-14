package com.normdevstorm.encryptedfiletransfer.server.controller;

import com.normdevstorm.encryptedfiletransfer.crypto.DES;
import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import com.normdevstorm.encryptedfiletransfer.utils.threads.SendFileThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerController extends GenericUIController {

    @FXML
    private Button sendBtn;
    @FXML
    private Button sendTextBtn;
    @FXML
    private TextArea messageInput;
    @FXML
    private TextField encryptionKeyInput;

    private File selectedFile;
    private ServerSocket serverSocket;
    private Stage stage;

    public void initializeController(ServerSocket serverSocket, Stage stage) {
        this.serverSocket = serverSocket;
        this.stage = stage;
        eventHandlers();
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

    private void sendEncryptedMessage(String message, String encryptionKey) {
        new Thread(() -> {
            try (Socket clientSocket = serverSocket.accept();
                 OutputStream output = clientSocket.getOutputStream();
                 PrintWriter writer = new PrintWriter(output, true)) {

                DES des = new DES();
                String binaryMessage = DES.utfToBin(message);
                String encryptedMessage = des.encrypt(encryptionKey, binaryMessage);
                String hexMessage = DES.binToHex(encryptedMessage);

                writer.println(hexMessage);
                Platform.runLater(() -> statusArea.appendText("Encrypted message (HEX) sent successfully!\n"));

            } catch (Exception e) {
                Platform.runLater(() -> statusArea.appendText("Error sending encrypted message: " + e.getMessage() + "\n"));
            }
        }).start();
    }
}
