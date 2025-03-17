package com.normdevstorm.encryptedfiletransfer.utils.threads;

import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.crypto.Rsa;
import com.normdevstorm.encryptedfiletransfer.model.FileModel;
import com.normdevstorm.encryptedfiletransfer.model.KeyModel;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import javafx.scene.control.TextArea;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;


import static com.normdevstorm.encryptedfiletransfer.crypto.Des.convertBytesToImage;
import static com.normdevstorm.encryptedfiletransfer.crypto.Des.hexStringToByteArray;

public class ReceiveFileThread extends Thread{
    private final FileType type;
    private final TextArea statusArea;
    private final Des des;
    private  Socket clientSocket;
    private final KeyModel keyModel = new KeyModel();


    public ReceiveFileThread(FileType type, TextArea statusArea, Des des, Socket clientSocket){
        this.statusArea = statusArea;
        this.type = type;
        this.des = des;
        this.clientSocket = clientSocket;
    }

    private String performHandShakeProtocol(Socket clientSocket, PrintWriter out, BufferedReader in) {
        try {
            // generate key pairs
            Rsa rsa = new Rsa();
            Map<String, BigInteger> keyPairs = rsa.generateKeyPairs("Hello from client");
            String publicKey = keyPairs.get("public_key").toString();
            String n = keyPairs.get("n_modulus").toString();
            String serverPublicKey;
            String encryptedKey;
            System.out.println("Private key: " + keyPairs.get("private_key"));
            System.out.println("Public key: " + keyPairs.get("public_key"));

            statusArea.appendText("Handshake protocol started !!! \n");
            String handShakeTrigger;
            while(!Objects.equals(handShakeTrigger = in.readLine(), "Start handshake protocol")){
                System.out.println("Wait for handshake trigger from server");
            }
            out.println("Yes");

            out.println(publicKey + "," + n);
            while((encryptedKey = in.readLine()) == null){
                System.out.println("Wait for server encrypted key");

            }
            System.out.println("Encrypted key: " + encryptedKey);
            String decryptedKey =  new String (Rsa.decrypt(new BigInteger(encryptedKey), keyPairs.get("private_key"), keyPairs.get("n_modulus")).toByteArray());
            System.out.println("Decrypted key: " + decryptedKey);

            statusArea.appendText("Received encrypted key from server: " + encryptedKey + "\n");
            statusArea.appendText("Handshake successfully !!! \n");

            return decryptedKey;


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void receiveFile(String message, String key){
        FileModel fileModel = FileModel.toModel(message);
        String encryptDataInHex = fileModel.getContent();
        String fileName = fileModel.getFile_name();
        FileType fileType = FileType.getTypeFromExtension(fileModel.getExtension());

        statusArea.appendText("Decrypting file: " + fileName + "\n");
        Des des = new Des();
        //TODO: to dynamically get the key
        String keyStr = key;
        byte[] keyBytes = keyStr.getBytes();
        byte[] encryptDataByte = hexStringToByteArray(encryptDataInHex);
        byte[] decryptedDataByte = des.encrypt(encryptDataByte, keyBytes, true);
        System.out.println("Decrypt content of file " + fileName + " is :" + new String(decryptedDataByte));
        processBasedOnTypeAndSaveFile(fileName, decryptedDataByte);
    }

    private void processBasedOnTypeAndSaveFile(String fileName, byte[] decryptDataByte){
        File outputFile = new File(fileName);
        statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(decryptDataByte);
        } catch (IOException e) {
            statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
        }
    }


    @Override
    public void run() {
        try {
            if(clientSocket.isClosed()){
                clientSocket = new Socket("localhost", 5000);
            }
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String key = performHandShakeProtocol(clientSocket, out,in);
//            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message = in.readLine();
            while (message == null ) {
                message = in.readLine();
            }
            String encryptedData = message;
            byte[] decryptedBytes;
            System.out.println("Message received: " + message);
            receiveFile(message, key);
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
