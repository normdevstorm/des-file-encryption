package com.normdevstorm.encryptedfiletransfer.utils.threads;
import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import com.normdevstorm.encryptedfiletransfer.crypto.Rsa;
import com.normdevstorm.encryptedfiletransfer.model.FileMetadata;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import javafx.scene.control.TextArea;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

public class SendFileThread extends Thread {
    private static final int CHUNK_SIZE = 65536; // 1 KB chunk size
    private static final int MAX_RETRIES = 6;
    private static final int ACK_TIMEOUT = 5000; // 5 seconds

    private File selectedFile;
    private FileType type;
    private final TextArea statusArea;
    private final ServerSocket serverSocket;
    private final String key;

    public SendFileThread(TextArea statusArea, ServerSocket serverSocket, String key) {
        this.statusArea = statusArea;
        this.serverSocket = serverSocket;
        this.key = key;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public TextArea getStatusArea() {
        return statusArea;
    }

    private boolean performHandShakeProtocol(BufferedReader in, BufferedWriter out, String key) {
        String clientPublicKeyWithNModulus = null;
        FileMetadata fileMetadata = new FileMetadata(selectedFile.getName(), selectedFile.length(), LocalDateTime.now());

        try {
            while (clientPublicKeyWithNModulus == null) {
                out.write("Start handshake protocol\n");
                out.flush();

                String handShakeConfirm;
                while ((handShakeConfirm = in.readLine()) == null || !handShakeConfirm.equals("Yes")) {
                    if (handShakeConfirm != null) {
                        System.out.println(handShakeConfirm);
                        System.out.println("Waiting for client to confirm");
                    }
                }
                // Send file metadata
                out.write(fileMetadata.toString() + "\n");
                out.flush();
                // Receive client Public key along with modulus
                while ((clientPublicKeyWithNModulus = in.readLine()) == null) {
                    System.out.println("Waiting for client to send public key with modulus");
                }
                System.out.println("Client public key with n: " + clientPublicKeyWithNModulus);
                // Extract Public Key and N modulus from client
                String[] parts = clientPublicKeyWithNModulus.split(",");
                BigInteger clientPublicKey = new BigInteger(parts[0]);
                BigInteger clientN = new BigInteger(parts[1]);
                System.out.println("Client public key: " + clientPublicKey.toString());
                System.out.println("N_modulus: " + clientN.toString());
                // Use client Public Key to encrypt DES key
                String encryptedKey = Rsa.encrypt(new BigInteger(key.getBytes()), clientPublicKey, clientN).toString();
                // Send encrypted DES key to client
                out.write(encryptedKey + "\n");
                out.flush();
                System.out.println("Encrypted key: " + encryptedKey);
                statusArea.appendText("Handshake successfully !!! \n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private byte[] encryptFile(BufferedReader in, BufferedWriter out, String key) throws Exception {

        Des des = new Des();
        byte[] keyBytes = key.getBytes();
        byte[] fileBytes;

        fileBytes = new byte[(int) selectedFile.length()];
        try (FileInputStream fis = new FileInputStream(selectedFile)) {
            fis.read(fileBytes);
        }

        statusArea.appendText("Started encrypting file: " + selectedFile.getName() + " at " + LocalDateTime.now() + "\n");
        byte[] result = des.encrypt(fileBytes, keyBytes, false);
        statusArea.appendText("Finished encrypting file: " + selectedFile.getName() + " at " + LocalDateTime.now() + "\n");

        return result;
    }

    @Override
    public void run() {
        try {
            Socket clientSocket;
            byte[] encryptedBytes;

            clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            if (performHandShakeProtocol(in, out, key)) {
                encryptedBytes = encryptFile(in, out, key);
        /// TODO: enable this code snippet for demo
//                File encryptedFile = new File("encrypted/encrypted_file.txt");
//
//                try(FileOutputStream fos = new FileOutputStream(encryptedFile)){
//                    fos.write(encryptedBytes);
//                }
                statusArea.appendText("Encrypted file: " + selectedFile.getName() + "\n");
                statusArea.appendText("Sending file: " + selectedFile.getName() + "\n");

                // Send the encrypted file in chunks
                sendFileInChunks(clientSocket, encryptedBytes);

                out.flush();
                statusArea.appendText("File sent successfully\n");
            }

            in.close();
            out.close();
        } catch (Exception e) {
            statusArea.appendText("Error sending file: " + e.getMessage() + "\n");
        }
    }

    private void sendFileInChunks(Socket clientSocket, byte[] encryptedBytes) {
        try (DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
             DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream())) {

            int totalBytesSent = 0;
            int retryCount = 0;

            while (totalBytesSent < encryptedBytes.length) {
                int bytesToSend = Math.min(CHUNK_SIZE, encryptedBytes.length - totalBytesSent);
                byte[] chunk = new byte[bytesToSend];
                System.arraycopy(encryptedBytes, totalBytesSent, chunk, 0, bytesToSend);

                boolean chunkAcknowledged = false;

                while (!chunkAcknowledged && retryCount < MAX_RETRIES) {
                    // Send the chunk
                    dataOutputStream.write(chunk, 0, bytesToSend);
                    dataOutputStream.flush();
                    totalBytesSent += bytesToSend;
                    System.out.println("Sent " + bytesToSend + " bytes. Total sent: " + totalBytesSent + "\n");

                    // Wait for ACK
                    try {
                        clientSocket.setSoTimeout(ACK_TIMEOUT);
                        String ack = dataInputStream.readUTF();
                        if (ack.startsWith("ACK")) {
                            System.out.println("Client acked: " + ack + "\n");
                            chunkAcknowledged = true;
                            retryCount = 0; // Reset retry count after successful ACK
                        } else {
                            statusArea.appendText("Unexpected response: " + ack + "\n");
                        }
                    } catch (IOException e) {
                        statusArea.appendText("Timeout waiting for ACK. Retrying...\n");
                        retryCount++;
                    }
                }

                if (retryCount >= MAX_RETRIES) {
                    statusArea.appendText("Max retries reached. Aborting file transfer.\n");
                    return;
                }
            }

            statusArea.appendText("File transfer completed successfully. Total bytes sent: " + totalBytesSent + "\n");

        } catch (IOException e) {
            statusArea.appendText("Error during file transfer: " + e.getMessage() + "\n");
        }
    }
}