package com.normdevstorm.encryptedfiletransfer;

import com.normdevstorm.encryptedfiletransfer.utils.encrypt.DES;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.normdevstorm.encryptedfiletransfer.utils.encrypt.DES.*;

public class FileTransferAppClient extends Application {
    private File selectedFile;
    private TextArea statusArea;


    @Override
    public void start(Stage primaryStage) throws IOException {
        // Tạo UI components
        Label titleLabel = new Label("File Encrypt & Transfer Client");
        titleLabel.getStyleClass().add("title-label");

        Button selectFileBtn = new Button("Choose File");
        selectFileBtn.getStyleClass().add("modern-button");

        Button sendBtn = new Button("Send Encrypted File");
        sendBtn.getStyleClass().add("modern-button");
        sendBtn.setDisable(true);

        Button receiveBtn = new Button("Receive & Decrypt");
        receiveBtn.getStyleClass().add("modern-button");

        statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.setPrefHeight(200);

        // Xử lý sự kiện
        FileChooser fileChooser = new FileChooser();
        selectFileBtn.setOnAction(e -> {
            selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                statusArea.appendText("Selected: " + selectedFile.getName() + "\n");
                sendBtn.setDisable(false);
            }
        });


        sendBtn.setOnAction(e -> sendFile(FileType.IMAGE));
        receiveBtn.setOnAction(e -> receiveFile(FileType.IMAGE));


        // Sắp xếp layout
        VBox layout = new VBox(20);
        layout.getStyleClass().add("container");
        layout.getChildren().addAll(titleLabel, selectFileBtn, sendBtn, receiveBtn, statusArea);

        // Tạo scene với CSS
        Scene scene = new Scene(layout, 400, 500);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        // Cấu hình stage
        primaryStage.setTitle("File Transfer Application");
        primaryStage.setScene(scene);
        primaryStage.show();
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
        if (enc) {
            if (message == null) {
                System.out.println("No message given to encrypt. Exiting..");
                System.exit(0);
            } else if (key1 == null) {
                System.out.println("Improper use of key arguments. Exiting..");
                System.exit(0);
            }

            if (key2 == null) {
                if (key3 != null) {
                    System.out.println("Improper use of key arguments. Exiting..");
                    System.exit(0);
                }
                result = des.encrypt(key1, message);
                /// TODO: gather all the convert format functions into a separate file
                if (type == FileType.IMAGE) {
                    try (FileOutputStream fos = new FileOutputStream("image_encrypted_hex.txt")) {
                        fos.write(DES.binToHex(result).getBytes());
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println(DES.binToHex(result));
            } else {
                if (key3 == null) {
                    System.out.println("Improper use of key arguments. Exiting..");
                    System.exit(0);
                }
                result = des.encrypt(key3, des.decrypt(key2, des.encrypt(key1, DES.utfToBin(message))));
                System.out.println(DES.binToHex(result));
            }
        } else {
            if (message == null) {
                System.out.println("No data given to decrypt. Exiting..");
                System.exit(0);
            } else if (key1 == null) {
                System.out.println("Improper use of key arguments. Exiting..");
                System.exit(0);
            }

            if (key2 == null) {
                if (key3 != null) {
                    System.out.println("Improper use of key arguments. Exiting..");
                    System.exit(0);
                }
                result = des.decrypt(key1, DES.hexToBin(message));
                System.out.println(DES.binToUTF(result));
            } else {
                if (key3 == null) {
                    System.out.println("Improper use of key arguments. Exiting..");
                    System.exit(0);
                }
                result = des.decrypt(key1, des.encrypt(key2, des.decrypt(key3, hexToBin(message))));
                System.out.println(DES.binToUTF(result));
            }
        }
        return DES.binToHex(result);
    }

    private void sendFile(FileType type) {
        new Thread(() -> {
            try {
                String encryptedData = encryptFile(selectedFile, type);
                /// todo: delete later on
//                System.out.println( "encryptedData: " + encryptedData );
                try (Socket socket = new Socket("localhost", 5000);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                ) {
                    out.println(encryptedData);
                    statusArea.appendText("File sent successfully\n");
                }
            } catch (Exception e) {
                statusArea.appendText("Error sending file: " + e.getMessage() + "\n");
            }
        }).start();
    }

    private void receiveFile(FileType type) {
        new Thread(() -> {
            DES des = new DES();
            try {
                Socket clientSocket = new Socket("localhost", 5000);
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
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}