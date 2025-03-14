package com.normdevstorm.encryptedfiletransfer.utils.threads;

import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.model.FileModel;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import javafx.scene.control.TextArea;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;


import static com.normdevstorm.encryptedfiletransfer.crypto.Des.convertBytesToImage;
import static com.normdevstorm.encryptedfiletransfer.crypto.Des.hexStringToByteArray;

public class ReceiveFileThread extends Thread{
    private final FileType type;
    private final TextArea statusArea;
    private final Des des;
    private final Socket clientSocket;


    public ReceiveFileThread(FileType type, TextArea statusArea, Des des, Socket clientSocket){
        this.statusArea = statusArea;
        this.type = type;
        this.des = des;
        this.clientSocket = clientSocket;
    }

    private void receiveFile(String message){

        FileModel fileModel = FileModel.toModel(message);
        String encryptDataInHex = fileModel.getContent();
        String fileName = fileModel.getFile_name();
        FileType fileType = FileType.getTypeFromExtension(fileModel.getExtension());

        Des des = new Des();
        //TODO: to dynamically get the key
        String keyStr = "key123456789";
        byte[] keyBytes = keyStr.getBytes();
        byte[] encryptDataByte = hexStringToByteArray(encryptDataInHex);
        byte[] decryptedDataByte = des.encrypt(encryptDataByte, keyBytes, true);

        processBasedOnTypeAndSaveFile(fileName, fileType, decryptedDataByte);
    }

    private void processBasedOnTypeAndSaveFile(String fileName, FileType type, byte[] decryptDataByte){
        switch (type) {
            case IMAGE:
                System.out.println("Decrypt content of image file " + fileName + " is :" + new String(decryptDataByte));
                try {
                    BufferedImage bufferedImage = convertBytesToImage(decryptDataByte);
                    File outputFile = new File( fileName +".png");
                    ImageIO.write(bufferedImage, "png", outputFile);
                    statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
                } catch (IOException e) {
                    statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
                }
                break;
            case TEXT:
            default:
                File outputFile = new File(fileName + ".txt");
                statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(decryptDataByte);
                } catch (IOException e) {
                    statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
                }
                break;
        }
    }


    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message = in.readLine();
            while (message != null) {
                String encryptedData = message;
                byte[] decryptedBytes;
                receiveFile(message);
                message = in.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
