package com.normdevstorm.encryptedfiletransfer.client.controller;

import com.normdevstorm.encryptedfiletransfer.crypto.DES;
import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientController extends GenericUIController {

    @FXML
    private Button receiveBtn;
    @FXML
    private Button decryptBtn;
    @FXML
    private TextArea statusArea;
    @FXML
    private TextField decryptionKeyInput;

    private String lastEncryptedMessage;

    public void initializeController() {
        eventHandlers();
    }

    @Override
    public void eventHandlers() {
        receiveBtn.setOnAction(actionEvent -> receiveMessage());
        decryptBtn.setOnAction(actionEvent -> decryptMessage());
    }

    private void receiveMessage() {
        new Thread(() -> {
            try (Socket clientSocket = new Socket("localhost", 5000);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

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
            DES des = new DES();
            String binaryMessage = DES.hexToBin(lastEncryptedMessage);
            String decryptedBinary = des.decrypt(providedKey, binaryMessage);
            String decryptedMessage = new String(DES.binToUTF(decryptedBinary).getBytes(), "UTF-8");

            // Giữ nguyên các khoảng trắng bằng cách bao chuỗi trong dấu ngoặc kép
            Platform.runLater(() -> statusArea.appendText("Decrypted message: " + decryptedMessage + "\n"));
        } catch (Exception e) {
            Platform.runLater(() -> statusArea.appendText("Decryption failed: Incorrect key or invalid data!\n"));
        }
    }
}
