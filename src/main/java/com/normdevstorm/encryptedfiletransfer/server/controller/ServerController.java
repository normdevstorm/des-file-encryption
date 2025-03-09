package com.normdevstorm.encryptedfiletransfer.server.controller;

import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import com.normdevstorm.encryptedfiletransfer.utils.threads.SendFileThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.ServerSocket;

public class ServerController extends GenericUIController {

    @FXML
    private Button sendBtn;
//    @FXML private Button receiveBtn;

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
                statusArea.appendText("Error occured when trying to choosing files !!!");
            }
        });

        sendBtn.setOnAction(e -> {
            try {
                sendFile(serverSocket);
            } catch (Exception ex) {
                statusArea.appendText("Error sending file: " + selectedFile.getName() + ex.getMessage());
            }
        });

    }

    private synchronized void sendFile(ServerSocket serverSocket) {
        if (selectedFile != null) {
            SendFileThread sendFileThread = new SendFileThread(statusArea, serverSocket);
            sendFileThread.setSelectedFile(selectedFile);
            sendFileThread.setType(FileType.IMAGE);
            Platform.runLater(sendFileThread::start);
        } else {
            /// TODO: add showDialog here
            System.out.println("File is null !!!");
        }

    }
}
