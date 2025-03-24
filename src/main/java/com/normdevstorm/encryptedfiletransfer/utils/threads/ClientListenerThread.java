package com.normdevstorm.encryptedfiletransfer.utils.threads;

import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.utils.constant.ConstantManager;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListenerThread extends Thread {
    private final TextArea statusArea;
    private final ServerSocket signalSocket;
    private final Socket fileSocket;
    private boolean running = true;

    public ClientListenerThread(TextArea statusArea, Socket fileSocket) throws IOException {
        this.statusArea = statusArea;
        this.fileSocket = fileSocket;
        this.signalSocket = new ServerSocket(ConstantManager.SIGNALING_PORT);
    }

    @Override
    public void run() {
        try {
            while (running) {
                Socket socket = signalSocket.accept();
                handleSignal(socket);
            }
        } catch (IOException e) {
            if (running) {
                Platform.runLater(() -> statusArea.appendText("Listener error: " + e.getMessage() + "\n"));
            }
        }
    }

    private void handleSignal(Socket socket) {
        new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String message = in.readLine();
                if (message != null && message.equals("Start handshake protocol")) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("File Transfer Request");
                        alert.setHeaderText("Incoming File");
                        alert.setContentText("Server wants to send a file. Do you accept?");

                        alert.showAndWait().ifPresent(buttonType -> {
                            try {
                                if (buttonType == ButtonType.OK) {
                                    statusArea.appendText("File transfer accepted\n");
                                    out.write("Yes\n");
                                    out.flush();

                                    // Create a separate thread to handle file receiving
                                    // to prevent blocking the UI thread
                                    new Thread(() -> {
                                        try {
//                                            Socket fileSocket = new Socket(ConstantManager.serverIpAddress,
//                                                    ConstantManager.FILE_TRANSFER_PORT);
                                            Des des = new Des();
//                                            statusArea.appendText("Starting file download...\n");
                                            ReceiveFileThread receiveFileThread = new ReceiveFileThread(
                                                    FileType.IMAGE, statusArea, des, fileSocket);
                                            receiveFileThread.start();
                                        } catch (Exception e) {
                                            Platform.runLater(() ->
                                                    statusArea.appendText("Error creating file connection: " +
                                                            e.getMessage() + "\n"));
                                        }
                                    }).start();
                                } else {
                                    statusArea.appendText("File transfer rejected\n");
                                    out.write("No\n");
                                    out.flush();
                                }
                            } catch (IOException e) {
                                statusArea.appendText("Error: " + e.getMessage() + "\n");
                            } finally {
                                try {
                                    // Make sure we close the signaling socket when done
                                    socket.close();
                                } catch (IOException e) {
                                    statusArea.appendText("Error closing signal socket: " + e.getMessage() + "\n");
                                }
                            }
                        });
                    });
                }
            } catch (IOException e) {
                Platform.runLater(() -> statusArea.appendText("Signal handling error: " + e.getMessage() + "\n"));
            }
        }).start();
    }
    public void stopListening() {
        running = false;
        try {
            if (!signalSocket.isClosed()) {
                signalSocket.close();
            }
        } catch (IOException e) {
            Platform.runLater(() -> statusArea.appendText("Error closing signal socket: " + e.getMessage() + "\n"));
        }
    }
}