package com.normdevstorm.encryptedfiletransfer.client.controller;

import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.crypto.RSA;
import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import com.normdevstorm.encryptedfiletransfer.model.KeyModel;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import com.normdevstorm.encryptedfiletransfer.utils.threads.ReceiveFileThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


public class ClientController extends GenericUIController {

    @FXML private Button receiveBtn;
    @FXML private Button handShakeRequest;
    @FXML private TextArea statusArea;
    static private Socket clientSocket;
    static private KeyModel keyModel;

    private Stage stage;

    public void initializeController() {
        try {
            keyModel = new KeyModel();
            clientSocket = new Socket("localhost", 5000);
             eventHandlers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void eventHandlers() {
        receiveBtn.setOnAction(actionEvent -> {
                    receiveFile(FileType.IMAGE, statusArea);
                }
        );
    }

    private synchronized void receiveFile(FileType type, TextArea statusArea) {
            Des des = new Des();
            ReceiveFileThread receiveFileThread = new ReceiveFileThread(FileType.IMAGE, statusArea, des, clientSocket);
            Platform.runLater(() -> {
                receiveFileThread.start();
            });
    }
}
