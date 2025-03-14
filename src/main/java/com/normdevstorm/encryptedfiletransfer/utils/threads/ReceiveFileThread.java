package com.normdevstorm.encryptedfiletransfer.utils.threads;

import com.normdevstorm.encryptedfiletransfer.crypto.DES;
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

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message = in.readLine();
            while (message != null) {
                String encryptedData = message;
                String decryptedString = "";
                byte[] decryptedBytes;
                switch (type) {
                    case IMAGE:
                        encryptedData = hexToBin(encryptedData);
                        String decryptedBin = des.decrypt("key1", encryptedData);
                        decryptedBytes = binToBytes(decryptedBin);
                        System.out.println("decryptedString: " + Arrays.toString(decryptedBytes));
                        try {
                            BufferedImage bufferedImage = convertBytesToImage(decryptedBytes);
                            File outputFile = new File("decrypted_received_file.png");
                            ImageIO.write(bufferedImage, "png", outputFile);
                            statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
                        } catch (IOException e) {
                            statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
                        }
                        break;
                    case TEXT:
                    default:
                        decryptedString = binToUTF(des.decrypt("key1", hexToBin(encryptedData)));
                        decryptedBytes = decryptedString.getBytes();
                        File outputFile = new File("decrypted_text_file");
                        statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            fos.write(decryptedBytes);
                        } catch (IOException e) {
                            statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
                        }
                        break;
                }
                message = in.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
