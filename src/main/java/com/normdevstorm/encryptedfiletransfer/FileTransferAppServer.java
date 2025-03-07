package com.normdevstorm.encryptedfiletransfer;

import com.normdevstorm.encryptedfiletransfer.utils.encrypt.DES;
import com.normdevstorm.encryptedfiletransfer.utils.enums.FileType;
import com.normdevstorm.encryptedfiletransfer.utils.threads.SendFileThread;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.normdevstorm.encryptedfiletransfer.utils.encrypt.DES.*;

public class FileTransferAppServer extends Application {
    private File selectedFile;
    private TextArea statusArea;


    @Override
    public void start(Stage primaryStage) throws IOException {
        // Tạo UI components
        Label titleLabel = new Label("File Encrypt & Transfer");
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
        // socket server
        ServerSocket serverSocket = new ServerSocket(5000);
        sendBtn.setOnAction(e -> {
            sendFile(serverSocket);
        });
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

//    private String encryptFile(File inputFile, FileType type) throws Exception {
//
//        DES des = new DES();
//        byte[] fileBytes;
//        String message = "";
//
//        switch (type) {
//            case FileType.IMAGE:
//                BufferedImage image = ImageIO.read(inputFile);
//                fileBytes = DES.convertImageToBytes(image);
//                StringBuilder messageBuilder = new StringBuilder();
//                for (byte b : fileBytes) {
//                    messageBuilder.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
//                }
//                message = messageBuilder.toString();
////            System.out.println( "message: " + message);
//                break;
//            case FileType.TEXT:
//            default:
//                fileBytes = new byte[(int) inputFile.length()];
//                try (FileInputStream fis = new FileInputStream(inputFile)) {
//                    fis.read(fileBytes);
//                    message = DES.utfToBin(new String(fileBytes, "UTF-8"));
//                }
//                break;
//        }
//
//
//        boolean enc = true;
//        ///TODO: read text from file and pass into message args
//        ///TODO: GUI
//        String key1 = "key1", key2 = null, key3 = null, result = null;
//
////       for (int i = 0; i < args.length; i++) {
////            if (args[i].equals("-k1"))
////                key1 = args[++i];
////            else if (args[i].equals("-k2"))
////                key2 = args[++i];
////            else if (args[i].equals("-k3"))
////                key3 = args[++i];
////            else if (args[i].equals("-m"))
////                message = args[++i];
////            else if (args[i].equals("-d"))
////                enc = false;
////        }
//
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
//                /// TODO: gather all the convert format functions into a separate file
//                if (type == FileType.IMAGE) {
//                    try (FileOutputStream fos = new FileOutputStream("image_encrypted_hex.txt")) {
//                        fos.write(DES.binToHex(result).getBytes());
//                    } catch (FileNotFoundException e) {
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                System.out.println(DES.binToHex(result));
//            } else {
//                if (key3 == null) {
//                    System.out.println("Improper use of key arguments. Exiting..");
//                    System.exit(0);
//                }
//                result = des.encrypt(key3, des.decrypt(key2, des.encrypt(key1, DES.utfToBin(message))));
//                System.out.println(DES.binToHex(result));
//            }
//        } else {
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
//        return DES.binToHex(result);
//    }


//    private void sendFile(FileType type) {
//        new Thread(() -> {
//            try {
//                String encryptedData = encryptFile(selectedFile, type);
//                statusArea.appendText("Encrypted file: " + selectedFile.getName() + "\n");
//                Socket clientSocket;
//                try (ServerSocket serverSocket = new ServerSocket(5000, 5)) {
//                    while (true) {
//                        clientSocket = serverSocket.accept();
//                        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
//                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

    /// /                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
//                        out.println(encryptedData);
//                        statusArea.appendText("File sent successfully\n");
//                    }
//                }
//            } catch (Exception e) {
//                statusArea.appendText("Error sending file: " + e.getMessage() + "\n");
//            }
//        }).start();
//    }
    private synchronized void  sendFile(ServerSocket serverSocket) {
        SendFileThread sendFileThread = new SendFileThread(statusArea, serverSocket);
        sendFileThread.setSelectedFile(selectedFile);
        sendFileThread.setType(FileType.IMAGE);
        Platform.runLater(sendFileThread::start);

    }

    private void receiveFile(FileType type) {
        new Thread(() -> {
//            try (ServerSocket serverSocket = new ServerSocket(5000)) {
//                statusArea.appendText("Waiting for file...\n");
//                try (Socket socket = serverSocket.accept();
//                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            DES des = new DES();
            String encryptedData = "298e55a4398bee29ff7ea1c89d98417ba2b27f178ed11dc41c58a1d529e966fb08b7c7de4836ec9043c7dd42be63a960d7bed40292b50dfbdcc52d50b66f45d89fea2586554849bcb4bc582d70be39b76ef9c3474688c03947f0f4d5cb7d126012e3b99e4120fb7c1510f9fcc493be029f83c07d951ea2157096710e3cda81808b9a7d5526a314a486f1052a3022fa6e2f66973bebcca18ee52dd9bd02cc4c73fff144e533e89e5a82e88a6e42f98df8bc130268d5e580b27bb9045fe3a44673b2e7d06813e9d09e1a1e966096b57447146b382db450ae9dd2b3c291e551aeec6e92c06c20405c4c0db4bb0c946aaa26fca088b4dc292600c3cd9264f9232d1d73f3d1437cbc3797fab1d7e05a660b44efa28f17112128b87a3785526d12585e1f5c0dfa0d81cca71fce42f7f0671697041d553ba7bc26237e48bb60b0b7f321e3b3de1f5118b411458302577155900983e6dee31e00e6c7e128c7d224eedcecbf42349fc4a022b2a7aeae1a61518eb56bb9025385ade39793a1a935d4ede9b70e3cd5dc9ca80c22fb8c16c3d753b9a249aa1fa8bb2c26f7f49b1b8dc952f13e6a1421b44c79f606a81670730255d48aeb203afa3918e0eb72a41943de1120d7ce7b38a7e930a51fe2e078f2e77a05ce03050314f40baff374a8d50412721e51e8408ff502ed7614e3e3094b356dbffdce2f38a3cb8c5f002ba591adb89e102f377913dcc3ccf8ee377913dcc3ccf8ee6d8766297e2ac24748d5141d11e002ab3c67e88733c317194b6798f84e9add26266474e6425726b98d1216e131ac3398f67f02075f435bf671f2d2fc0d3dce6e952210e840471d7ef82253fd999600cebd08a7474c10ebbce346b995917d5ead2eb5ddbeb9c221e0f8f8443d292f7f1c24fc7ef3044a8dbe6a1697edfefd07ecd32e914845feb339fc5a4e7c1898f40d381d95feca12af52a5047342ff67494a5d7e0fe8ad0aa28369e04ee355ed2286df044619f35510eed70751f7301504b0b8b7b9365b31d3cf6d7cf8872c6aa573dc6f3281d9664e2555c8e6c6fadf2675d703c5b9798964055c486ee2e86532d66df0a34dfa9cba2100094274b553de335a6c0dcd71eeb1cef600ebdb5d92934611237758391a7ccca062230c66fc693cd149c862e6766c7e6d42c2c96477e8d74235e76301aeb04e22bfd42858bf5850e2f16635978e9614385821f5c99ac662a650f9b001b611311244da21ffb84487095bb21b0c908121f30acc02c41971210d2bb4b81e5b66e00b3ae308d9dddc2946e5f32d7dea23ecb769d2e60fd14a2891c234ad7c25f353333c70c02206741d4f69d2257ff1584ba253684b2480a7f0b41e1969bf53660e29511e7bbdf6bcfb64c83cd273bdc71f79e14e0fa31756c08d21f3a01a9f2fc6244d6b2c188c2a668f136072fe572921a74d4a65b5e285feced7132cce3ac5314d2b0312bfa8a29257aaf44b4c111e94950dc8a0ae9301d234e3644d303c14237937282ccecde14e93fc62fa2596d2fd9c9a0ecf55834cd4b004c7ddf790d5ea0eb6a1eb1585aed24fb7d42fcf2d9b220080b3a97b7f1c0defc7eaa6a728f22f939043a0509c5135ad41ab557f903e418b9d26777f621b597b78497f420421b75af14904bbf3cfd9bc6abbe11dff3bd71805133a6b2c6c1f0280c8b4d01a23611b79fb7cd6af09630ee4f8ed26f8f806cb5ef472b87c97d8ff0a46a164c5ccaca4c7740af58ac051b6f97efac763e343e1501e5a34be6c9b3a2df143d8d77d8dd02445f7cee7aea343ef604a70a7375ecce65306394031e8569b6abaa920eb178e6666aa3fa60b1466ca07c2e29965183e0bbb606ccb8e45cdd358521d35328fdcfa218095e26e83294bbe967734cf4641476d2ac66c37cf9d562abd7457d886fe11118df1f09a77149c1d285ea693652458649d713519c644b767230cf0985ee2f5bb1e52a2d1f89657eb922979fd3677f2ee38cbdedc8d1b5a221455362ce256269b9b0713ca93b84c1c6c3e177ebefa9d2b1792501fa767a9515bd442d391ab78cb7270092311c5dd35dd24ccd5cff4d04cb7ab3a6baac1e45937b704a420d97266118a4e44210491406397a5cea22509d5cbfdb824d60e7ba240858ee1cf17b855a45f5bfcfd6c19e465ebc38458de714c7540e6e898d5cfbc38ef047af75bef4e789330cbb78c9cfc051d78a3b69dc14f5438fff7d6b7617eb9edafac1755379359561bdbe3038c3586e23dac6ff64d727999cf9311881331f02ced9840";
            String decryptedString = "";
            byte[] decryptedBytes;
            switch (type) {
                case IMAGE:
                    try {
                        encryptedData = new String(Files.readAllBytes(Paths.get("image_encrypted_hex.txt")));
                        encryptedData = hexToBin(encryptedData);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String decryptedBin = des.decrypt("key1", encryptedData);
                    decryptedBytes = binToBytes(decryptedBin);
                    System.out.println("decryptedString: " + Arrays.toString(decryptedBytes));
                    try {
                        BufferedImage bufferedImage = convertBytesToImage(decryptedBytes);
                        File outputFile = new File("decrypted_" + selectedFile.getName());
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
                    File outputFile = new File("decrypted_" + selectedFile.getName());
                    statusArea.appendText("File received and decrypted: " + outputFile.getName() + "\n");
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        fos.write(decryptedBytes);
                    } catch (IOException e) {
                        statusArea.appendText("Error receiving file: " + e.getMessage() + "\n");
                    }
                    break;
            }

        }).start();
    }

    public static void main(String[] args) {

        launch(args);

    }
}