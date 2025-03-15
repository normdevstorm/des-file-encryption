    package com.normdevstorm.encryptedfiletransfer.utils.threads;

    import com.normdevstorm.encryptedfiletransfer.crypto.Des;
    import com.normdevstorm.encryptedfiletransfer.crypto.RSA;
    import com.normdevstorm.encryptedfiletransfer.model.FileModel;
    import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;

    import javax.imageio.ImageIO;
    import java.awt.image.BufferedImage;
    import java.io.*;
    import java.math.BigInteger;
    import java.net.ServerSocket;
    import java.net.Socket;
    import java.time.LocalDateTime;
    import java.util.Arrays;
    import java.util.List;
    import java.util.Objects;

    import javafx.scene.control.TextArea;

    import static com.normdevstorm.encryptedfiletransfer.crypto.Des.byteArrayToHexString;
    import static com.normdevstorm.encryptedfiletransfer.crypto.Des.convertImageToBytes;

    public class SendFileThread extends Thread {
        private File selectedFile;
        private FileType type;
        private final TextArea statusArea;
        private final ServerSocket serverSocket;


        public SendFileThread(TextArea statusArea, ServerSocket serverSocket) {
            this.statusArea = statusArea;
            this.serverSocket = serverSocket;
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

        private String performHandShakeProtocol(BufferedReader in, PrintWriter out) {
            String clientPublicKeyWithNModulus = null;
            //TODO: dynamically assign
            String key = "Default Message !!!";
            try {
                RSA rsa = new RSA();
                Socket clientSocket = null;
                // generate key pairs
                while (clientPublicKeyWithNModulus == null) {
//                    clientSocket = serverSocket.accept();
                    out.println("Start handshake protocol");
                    String handShakeConfirm;
                    while(!Objects.equals(handShakeConfirm = in.readLine(), "Yes")){
                        System.out.println("Waiting for client to confirm");
                    }
//                        out.println(keyPairs.get("public_key"));
                    while ((clientPublicKeyWithNModulus = in.readLine()) == null){
                    }
                    System.out.println("Client public key with n: " + clientPublicKeyWithNModulus);

                    //TODO: del later on
                    String[] parts = clientPublicKeyWithNModulus.split(",");
                    BigInteger clientPublicKey = new BigInteger(parts[0]);
                    BigInteger clientN = new BigInteger(parts[1]);
                    System.out.println("Client public key: " + clientPublicKey.toString());
                    System.out.println("N_modulus: " + clientN.toString());

                    String encryptedKey = RSA.encrypt(new BigInteger(key.getBytes()), clientPublicKey, clientN).toString();

                    out.println(encryptedKey);
                    System.out.println("Encrypted key: " + encryptedKey);

                     statusArea.appendText("Handshake successfully !!! \n");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return key;
        }

        private String encryptFile(File inputFile, FileType type,BufferedReader in,  PrintWriter out) throws Exception {
            String clientPublicKey = performHandShakeProtocol(in, out);

            if(clientPublicKey == null){
                System.out.println("Failed to handshake !!!");
            }
//            out.println("Start sending file");

            Des des = new Des();
            String keyStr = clientPublicKey;

            byte[] keyBytes = keyStr.getBytes();
            byte[] fileBytes;

            switch (type) {
                case FileType.IMAGE:
                    BufferedImage image = ImageIO.read(inputFile);
                    fileBytes = convertImageToBytes(image);
                    break;
                case FileType.TEXT:
                default:
                    fileBytes = new byte[(int) inputFile.length()];
                    try (FileInputStream fis = new FileInputStream(inputFile)) {
                        fis.read(fileBytes);
                    }
                    break;
            }
            statusArea.appendText("Start encrypting file: " + inputFile.getName() + "\n");
            byte[] encryptedBytes = des.encrypt(fileBytes, keyBytes, false);
            StringBuilder encryptedHex = byteArrayToHexString(encryptedBytes);
            System.out.println("Encrypted (hex): " + encryptedHex);
            return encryptedHex.toString();
        }

        private String processFile(BufferedReader in, PrintWriter out) throws Exception {
            // metadata
            List<String> fileNameWithExt = Arrays.stream(selectedFile.getName().split("\\.")).toList();
            String fileName = fileNameWithExt.get(0);
            String extension = fileNameWithExt.get(1);
            FileType fileType = FileType.getTypeFromExtension(extension);
            Long size = selectedFile.getTotalSpace();
            String content = encryptFile(selectedFile, fileType, in, out);
            // wrap with file model
            FileModel fileModel = new FileModel(fileName, extension, size, LocalDateTime.now(),content);
            return fileModel.toString();
        }

        @Override
        public void run() {
                try {
                Socket clientSocket;
                clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String encryptedData = processFile(in, out);
                statusArea.appendText("Encrypted file: " + selectedFile.getName() + "\n");
                statusArea.appendText("Sending file: " + selectedFile.getName() + "\n");
                    //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                    out.println(encryptedData);
                    statusArea.appendText("File sent successfully\n");
                    in.close();
                    out.close();
            } catch (Exception e) {
                statusArea.appendText("Error sending file: " + e.getMessage() + "\n");
            }
        }
    }
