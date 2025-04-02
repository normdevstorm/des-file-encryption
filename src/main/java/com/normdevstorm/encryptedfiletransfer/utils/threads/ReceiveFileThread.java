package com.normdevstorm.encryptedfiletransfer.utils.threads;
import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.crypto.Rsa;
import com.normdevstorm.encryptedfiletransfer.model.FileMetadata;
import com.normdevstorm.encryptedfiletransfer.model.KeyModel;
import com.normdevstorm.encryptedfiletransfer.utils.constant.ConstantManager;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Map;

public class ReceiveFileThread extends Thread {
    private static final int CHUNK_SIZE = 65536; // 1 KB chunk size
    private static final int ACK_TIMEOUT = 5000; // 5 seconds

    private final FileType type;
    private final TextArea statusArea;
    private final Des des;
    private Socket clientSocket;
    private final KeyModel keyModel = new KeyModel();
    private final FileMetadata fileMetadata = new FileMetadata();

    public ReceiveFileThread(FileType type, TextArea statusArea, Des des, Socket clientSocket) {
        this.statusArea = statusArea;
        this.type = type;
        this.des = des;
        this.clientSocket = clientSocket;
    }

    private String performHandShakeProtocol(Socket clientSocket, BufferedWriter out, BufferedReader in) {
        try {
            // generate key pairs
            Rsa rsa = new Rsa();
            Map<String, BigInteger> keyPairs = rsa.generateKeyPairs("Hello from client");
            String publicKey = keyPairs.get("public_key").toString();
            String n = keyPairs.get("n_modulus").toString();
            String serverPublicKey;
            String encryptedKey;
            String handShakeTrigger;
            String fileMetaString;

            System.out.println("Private key: " + keyPairs.get("private_key"));
            System.out.println("Public key: " + keyPairs.get("public_key"));

            statusArea.appendText("Handshake protocol started !!! \n");
            while ((handShakeTrigger = in.readLine()) == null || !handShakeTrigger.equals("Start handshake protocol")) {
                if (handShakeTrigger != null) {
                    System.out.println(handShakeTrigger);
                    System.out.println("Wait for handshake trigger from server");
                }
            }

            out.write("Yes\n");
            out.flush();

            // Get file metadata
            while ((fileMetaString = in.readLine()) == null) {
                System.out.println("Waiting for server sending metadata");
            }
            FileMetadata receivedFileMetadata = FileMetadata.toModel(fileMetaString);
            fileMetadata.setFile_name(receivedFileMetadata.getFile_name());
            fileMetadata.setSize(receivedFileMetadata.getSize());
            fileMetadata.setTime_sent(receivedFileMetadata.getTime_sent());
            System.out.println(fileMetadata);

            // Send client Public Key and N modulus
            out.write(publicKey + "," + n + "\n");
            out.flush();

            // Receive and decrypted DES key using Client Private Key
            while ((encryptedKey = in.readLine()) == null) {
                System.out.println("Wait for server encrypted key");
            }
            String decryptedKey = new String(Rsa.decrypt(new BigInteger(encryptedKey), keyPairs.get("private_key"), keyPairs.get("n_modulus")).toByteArray());
            System.out.println("Decrypted key: " + decryptedKey);

            statusArea.appendText("Received and decrypted key from server\n");
            statusArea.appendText("Handshake successfully !!! \n");
            return decryptedKey;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveFile(byte[] encryptedByte, String key) {
        String fileName = fileMetadata.getFile_name();
        statusArea.appendText("Decrypting file: " + fileName + "\n");
        Des des = new Des();
        byte[] keyBytes = key.getBytes();
        byte[] decryptedDataByte = des.encrypt(encryptedByte, keyBytes, true);
//        System.out.println("Decrypt content of file " + fileName + " is :" + new String(decryptedDataByte));
        processBasedOnTypeAndSaveFile(fileName, decryptedDataByte);

        Platform.runLater(() -> {
            showResultAlert("File Transfer Complete",
                    "File successfully received and decrypted: " + fileName);
        });
    }

    private void processBasedOnTypeAndSaveFile(String fileName, byte[] decryptDataByte) {
        File outputFile = new File(fileName);
        statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(decryptDataByte);
        } catch (IOException e) {
            statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
        }
    }

    private byte[] receiveFileInChunks() {
        try (DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream())) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[CHUNK_SIZE];
            int totalBytesReceived = 0;
            int defaultRemainingBytes = Math.min(fileMetadata.getSize().intValue(), CHUNK_SIZE);

            while (totalBytesReceived < fileMetadata.getSize()) {
                int remainingBytesOfWholeFile = fileMetadata.getSize().intValue() - totalBytesReceived;
                int bytesRead = 0;
                int remainingBytes = Math.min(remainingBytesOfWholeFile, defaultRemainingBytes);

                // Read until the full chunk is received
                while (remainingBytes > 0) {
                    int read = dataInputStream.read(buffer, bytesRead, remainingBytes);
                    if (read == -1) {
                        break; // End of stream
                    }
                    bytesRead += read;
                    remainingBytes -= read;
                }

                if (bytesRead > 0) {
                    // Write the chunk to the output stream
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    totalBytesReceived += bytesRead;
                    System.out.println("Received " + bytesRead + " bytes. Total received: " + totalBytesReceived + "\n");

                    // Send ACK
                    dataOutputStream.writeUTF("ACK " + totalBytesReceived);
                    dataOutputStream.flush();
                }
            }

            if (fileMetadata.getSize() == totalBytesReceived) {
                statusArea.appendText("File transfer completed successfully.\n");
            } else {
                statusArea.appendText("File transfer incomplete. Expected: " + fileMetadata.getSize() + ", Received: " + totalBytesReceived + "\n");
            }

            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            statusArea.appendText("Error during file transfer: " + e.getMessage() + "\n");
            return new byte[0];
        }
    }

    private void showResultAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void run() {
        try {
            if (clientSocket.isClosed()) {
                clientSocket = new Socket(ConstantManager.serverIpAddress, 5000);
            }

            String key;
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            key = performHandShakeProtocol(clientSocket, out, in);

            byte[] encryptedBytes = receiveFileInChunks();
            receiveFile(encryptedBytes, key);

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}