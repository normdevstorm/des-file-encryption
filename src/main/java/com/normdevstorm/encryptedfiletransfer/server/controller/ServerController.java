package com.normdevstorm.encryptedfiletransfer.server.controller;
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
    }

    private synchronized void sendFile(ServerSocket serverSocket) {
        if (selectedFile != null) {
            Platform.runLater(() -> {
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

                                // Start the actual file transfer on the original port
                                SendFileThread sendFileThread = new SendFileThread(statusArea, serverSocket);
                                sendFileThread.setSelectedFile(selectedFile);
                                sendFileThread.setType(FileType.IMAGE);
                                sendFileThread.start();
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
            });
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Send File Warning");
                alert.setHeaderText("No File Selected");
                alert.setContentText("Please select a file before sending.");
                alert.showAndWait();
                statusArea.appendText("No file selected for sending\n");
            });
        }
    }
}
