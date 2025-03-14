package com.normdevstorm.encryptedfiletransfer.client.controller;

import com.normdevstorm.encryptedfiletransfer.crypto.DES;
import com.normdevstorm.encryptedfiletransfer.model.GenericUIController;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import com.normdevstorm.encryptedfiletransfer.utils.threads.ReceiveFileThread;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


import static com.normdevstorm.encryptedfiletransfer.crypto.DES.*;

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
            DES des = new DES();
            ReceiveFileThread receiveFileThread = new ReceiveFileThread(FileType.IMAGE, statusArea, des, clientSocket);
            Platform.runLater(() -> {
                receiveFileThread.start();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        new Thread(() -> {
//            DES des = new DES();
//            try {
//                Socket clientSocket = new Socket("localhost", 5000);
//                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                String message = in.readLine();
//                while (message != null) {
//                    String encryptedData = message;
//                    String decryptedString = "";
//                    byte[] decryptedBytes;
//                    switch (type) {
//                        case IMAGE:
//                            encryptedData = hexToBin(encryptedData);
//                            String decryptedBin = des.decrypt("key1", encryptedData);
//                            decryptedBytes = binToBytes(decryptedBin);
//                            System.out.println("decryptedString: " + Arrays.toString(decryptedBytes));
//                            try {
//                                BufferedImage bufferedImage = convertBytesToImage(decryptedBytes);
//                                File outputFile = new File("decrypted_received_file.png");
//                                ImageIO.write(bufferedImage, "png", outputFile);
//                                statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
//                            } catch (IOException e) {
//                                statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
//                            }
//                            break;
//                        case TEXT:
//                        default:
//                            decryptedString = binToUTF(des.decrypt("key1", hexToBin(encryptedData)));
//                            decryptedBytes = decryptedString.getBytes();
//                            File outputFile = new File("decrypted_text_file");
//                            statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
//                            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
//                                fos.write(decryptedBytes);
//                            } catch (IOException e) {
//                                statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
//                            }
//                            break;
//                    }
//                    message = in.readLine();
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();
    }
}
