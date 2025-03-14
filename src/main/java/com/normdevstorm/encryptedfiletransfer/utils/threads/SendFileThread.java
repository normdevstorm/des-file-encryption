    package com.normdevstorm.encryptedfiletransfer.utils.threads;

    import com.normdevstorm.encryptedfiletransfer.crypto.Des;
    import com.normdevstorm.encryptedfiletransfer.model.FileModel;
    import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;

    import javax.imageio.ImageIO;
    import java.awt.image.BufferedImage;
    import java.io.*;
    import java.net.ServerSocket;
    import java.net.Socket;
    import java.time.LocalDateTime;
    import java.util.Arrays;
    import java.util.List;

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

        private String encryptFile(File inputFile, FileType type) throws Exception {

            Des des = new Des();
            //TODO: to dynamically get the key
            String keyStr = "key123456789";

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

            byte[] encryptedBytes = des.encrypt(fileBytes, keyBytes, false);
            StringBuilder encryptedHex = byteArrayToHexString(encryptedBytes);
            System.out.println("Encrypted (hex): " + encryptedHex);
            return encryptedHex.toString();

            ///TODO: read text from file and pass into message args
            ///TODO: GUI

        }

        private String processFile() throws Exception {
            // metadata
            List<String> fileNameWithExt = Arrays.stream(selectedFile.getName().split("\\.")).toList();
            String fileName = fileNameWithExt.get(0);
            String extension = fileNameWithExt.get(1);
            FileType fileType = FileType.getTypeFromExtension(extension);
            Long size = selectedFile.getTotalSpace();
            String content = encryptFile(selectedFile, fileType);
            // wrap with file model
            FileModel fileModel = new FileModel(fileName, extension, size, LocalDateTime.now(),content);
            return fileModel.toString();
        }

        @Override
        public void run() {
            // encrypt and send files right below
            try {
                statusArea.appendText("Encrypting file: " + selectedFile.getAbsolutePath() + "\n");
                String encryptedData = processFile();
                statusArea.appendText("Encrypted file: " + selectedFile.getName() + "\n");
                statusArea.appendText("Sending file: " + selectedFile.getName() + "\n");
                    Socket clientSocket;
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                    out.println(encryptedData);
                    statusArea.appendText("File sent successfully\n");
            } catch (Exception e) {
                statusArea.appendText("Error sending file: " + e.getMessage() + "\n");
            }
        }
    }
