package com.normdevstorm.encryptedfiletransfer.utils.threads;

import com.normdevstorm.encryptedfiletransfer.crypto.DES;
import com.normdevstorm.encryptedfiletransfer.model.FileModel;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import javafx.scene.control.TextArea;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import static com.normdevstorm.encryptedfiletransfer.crypto.DES.*;

public class ReceiveFileThread extends Thread{
    private final FileType type;
    private final TextArea statusArea;
    private final DES des;
    private final Socket clientSocket;


    public ReceiveFileThread(FileType type, TextArea statusArea, DES des, Socket clientSocket){
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

        String decryptedData = des.decrypt("key1", hexToBin(encryptDataInHex));
        processBasedOnTypeAndSaveFile(fileName, fileType, decryptedData);
    }

    private void processBasedOnTypeAndSaveFile(String fileName, FileType type, String decryptDataInBinary){
        byte[] decryptedBytes;
        switch (type) {
            case IMAGE:
                decryptedBytes = binToBytes(decryptDataInBinary);
                System.out.println("Decrypt content of image file " + fileName + " is :" + Arrays.toString(decryptedBytes));
                try {
                    BufferedImage bufferedImage = convertBytesToImage(decryptedBytes);
                    File outputFile = new File( fileName +".png");
                    ImageIO.write(bufferedImage, "png", outputFile);
                    statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
                } catch (IOException e) {
                    statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
                }
                break;
            case TEXT:
            default:
                String decryptedString = binToUTF(decryptDataInBinary);
                decryptedBytes = decryptedString.getBytes();
                File outputFile = new File(fileName + ".txt");
                statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(decryptedBytes);
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
