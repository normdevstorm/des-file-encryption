    package com.normdevstorm.encryptedfiletransfer.utils.threads;

    import com.normdevstorm.encryptedfiletransfer.crypto.DES;
    import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;

    import javax.imageio.ImageIO;
    import java.awt.image.BufferedImage;
    import java.io.*;
    import java.net.ServerSocket;
    import java.net.Socket;

    import javafx.scene.control.TextArea;

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

            DES des = new DES();
            byte[] fileBytes;
            String message = "";

            switch (type) {
                case FileType.IMAGE:
                    BufferedImage image = ImageIO.read(inputFile);
                    fileBytes = DES.convertImageToBytes(image);
                    StringBuilder messageBuilder = new StringBuilder();
                    for (byte b : fileBytes) {
                        messageBuilder.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
                    }
                    message = messageBuilder.toString();
    //            System.out.println( "message: " + message);
                    break;
                case FileType.TEXT:
                default:
                    fileBytes = new byte[(int) inputFile.length()];
                    try (FileInputStream fis = new FileInputStream(inputFile)) {
                        fis.read(fileBytes);
                        message = DES.utfToBin(new String(fileBytes, "UTF-8"));
                    }
                    break;
            }


            boolean enc = true;
            ///TODO: read text from file and pass into message args
            ///TODO: GUI
            String key1 = "key1", key2 = null, key3 = null, result = null;
            result = des.encrypt(key1, message);
            System.out.println(DES.binToHex(result));


    //       for (int i = 0; i < args.length; i++) {
    //            if (args[i].equals("-k1"))
    //                key1 = args[++i];
    //            else if (args[i].equals("-k2"))
    //                key2 = args[++i];
    //            else if (args[i].equals("-k3"))
    //                key3 = args[++i];
    //            else if (args[i].equals("-m"))
    //                message = args[++i];
    //            else if (args[i].equals("-d"))
    //                enc = false;
    //        }

    //        if (enc) {
    //            if (message == null) {
    //                System.out.println("No message given to encrypt. Exiting..");
    //                System.exit(0);
    //            } else if (key1 == null) {
    //                System.out.println("Improper use of key arguments. Exiting..");
    //                System.exit(0);
    //            }
    //
    //            if (key2 == null) {
    //                if (key3 != null) {
    //                    System.out.println("Improper use of key arguments. Exiting..");
    //                    System.exit(0);
    //                }
    //                result = des.encrypt(key1, message);
    ////                /// TODO: gather all the convert format functions into a separate file, for the purpose of debugging
    ////                if (type == FileType.IMAGE) {
    ////                    try (FileOutputStream fos = new FileOutputStream("image_encrypted_hex.txt")) {
    ////                        fos.write(DES.binToHex(result).getBytes());
    ////                    } catch (FileNotFoundException e) {
    ////                        throw new RuntimeException(e);
    ////                    } catch (IOException e) {
    ////                        throw new RuntimeException(e);
    ////                    }
    ////                }
    //                System.out.println(DES.binToHex(result));
    //            } else {
    //                if (key3 == null) {
    //                    System.out.println("Improper use of key arguments. Exiting..");
    //                    System.exit(0);
    //                }
    //                result = des.encrypt(key3, des.decrypt(key2, des.encrypt(key1, DES.utfToBin(message))));
    //                System.out.println(DES.binToHex(result));
    //            }
    //        }
    //        else {
    //            if (message == null) {
    //                System.out.println("No data given to decrypt. Exiting..");
    //                System.exit(0);
    //            } else if (key1 == null) {
    //                System.out.println("Improper use of key arguments. Exiting..");
    //                System.exit(0);
    //            }
    //
    //            if (key2 == null) {
    //                if (key3 != null) {
    //                    System.out.println("Improper use of key arguments. Exiting..");
    //                    System.exit(0);
    //                }
    //                result = des.decrypt(key1, DES.hexToBin(message));
    //                System.out.println(DES.binToUTF(result));
    //            } else {
    //                if (key3 == null) {
    //                    System.out.println("Improper use of key arguments. Exiting..");
    //                    System.exit(0);
    //                }
    //                result = des.decrypt(key1, des.encrypt(key2, des.decrypt(key3, hexToBin(message))));
    //                System.out.println(DES.binToUTF(result));
    //            }
    //        }
            return DES.binToHex(result);
        }


        @Override
        public void run() {
            // encrypt and send files right below
            try {
                statusArea.appendText("Encrypting file: " + selectedFile.getAbsolutePath() + "\n");
                String encryptedData = encryptFile(selectedFile, type);
                statusArea.appendText("Encrypted file: " + selectedFile.getName() + "\n");
                statusArea.appendText("Sending file: " + selectedFile.getName() + "\n");
                    Socket clientSocket;
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    //                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                    out.println(encryptedData);
                    statusArea.appendText("File sent successfully\n");


            } catch (Exception e) {
                statusArea.appendText("Error sending file: " + e.getMessage() + "\n");
            }
        }
    }
