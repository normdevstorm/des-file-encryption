package com.normdevstorm.encryptedfiletransfer.client.controller;

import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import com.normdevstorm.encryptedfiletransfer.utils.threads.ReceiveFileThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import java.io.*;
import java.net.Socket;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


public class ClientController extends GenericUIController {

    @FXML private Button receiveBtn;
    @FXML private TextArea statusArea;

    private Stage stage;

    public void initializeController() {
        eventHandlers();
    }


    @Override
    public void eventHandlers() {
        receiveBtn.setOnAction(actionEvent -> {
                    receiveFile(FileType.IMAGE, statusArea);
                }
        );


    }

    private void receiveFile(FileType type, TextArea statusArea) {
        try {
            Socket clientSocket = new Socket("localhost", 5000);
            Des des = new Des();
            ReceiveFileThread receiveFileThread = new ReceiveFileThread(FileType.IMAGE, statusArea, des, clientSocket);
            Platform.runLater(() -> {
                receiveFileThread.start();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
