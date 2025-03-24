package com.normdevstorm.encryptedfiletransfer.client.controller;

import com.normdevstorm.encryptedfiletransfer.crypto.Des;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class ClientController {

    @FXML private TextArea receivedMessageArea;

    public void initializeController() {
        new Thread(this::receiveEncryptedMessage).start();
    }

    private void receiveEncryptedMessage() {
        boolean wasConnected = false;

        while (true) {
            try {
                Socket socket = new Socket("localhost", 5050);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if (!wasConnected) {
                    wasConnected = true;
                    Platform.runLater(() -> receivedMessageArea.appendText("Connected to server!\n"));
                }

                String encryptedHex;
                while ((encryptedHex = reader.readLine()) != null) { // Nhận dữ liệu liên tục
                    String finalEncryptedHex = encryptedHex;
                    Platform.runLater(() -> {
                        showMessageDialog(finalEncryptedHex);
                        receivedMessageArea.appendText("Waiting for server...\n");
                    });
                }
                wasConnected = false;
                Platform.runLater(() -> receivedMessageArea.appendText("Disconnect or server error\n"));
                socket.close();

            } catch (Exception e) {
                if (wasConnected) { // Chỉ in nếu trước đó đang kết nối mà bị mất
                    wasConnected = false;
                    Platform.runLater(() -> receivedMessageArea.appendText("Disconnect or server error\n"));
                }

                try {
                    Thread.sleep(2000); // Chờ 2 giây rồi thử lại
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void showMessageDialog(String encryptedHex) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Encrypted Message Received");
        alert.setHeaderText("You have received an encrypted message.");
        alert.setContentText("Do you want to decrypt it?");

        ButtonType decryptButton = new ButtonType("Decrypt");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(decryptButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == decryptButton) {
            requestDecryptionKey(encryptedHex);
        }
    }

    private void requestDecryptionKey(String encryptedHex) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Decryption Key");
        dialog.setHeaderText("Enter the decryption key:");
        dialog.setContentText("Key:");

        Optional<String> keyInput = dialog.showAndWait();
        keyInput.ifPresent(key -> {
            if (key.trim().isEmpty()) { // Kiểm tra nếu để trống
                receivedMessageArea.appendText("Invalid key! Cannot be empty.\n");
            } else {
                decryptMessage(encryptedHex, key);
            }
        });
    }


//    private void decryptMessage(String encryptedHex, String decryptionKey) {
//        try {
//            // Chuyển từ HEX -> byte[]
//            byte[] encryptedBytes = hexToBytes(encryptedHex);
//            byte[] keyBytes = decryptionKey.getBytes(StandardCharsets.UTF_8);
//
//            // Đảm bảo key có đúng 8 bytes
//            keyBytes = adjustKeyLength(keyBytes, 8);
//
//            // Giải mã
//            Des des = new Des();
//            byte[] decryptedBytes = des.decrypt(encryptedBytes, keyBytes);
//
//            // Kiểm tra dữ liệu sau giải mã có hợp lệ không
//            String decryptedMessage;
//            try {
//                decryptedMessage = new String(decryptedBytes, StandardCharsets.UTF_8);
//            } catch (Exception utf8Error) {
//                throw new Exception("Key Invalid Input Error"); // Nếu giải mã rác, báo lỗi
//            }
//
//            // Nếu giải mã thành công -> Hiển thị nội dung
//            String finalMessage = decryptedMessage;
//            Platform.runLater(() -> {
//                receivedMessageArea.appendText("Encrypted HEX: " + encryptedHex + "\n");
//                receivedMessageArea.appendText("Decrypted Message: " + finalMessage + "\n");
//            });
//        }  catch (Exception e) {
//            // Nếu lỗi, chỉ in ra mã HEX và lỗi "Key Invalid Input Error"
//            Platform.runLater(() -> {
//                receivedMessageArea.appendText("Decryption failed: " + e.getMessage() + "\n");
//                receivedMessageArea.appendText("Encrypted HEX: " + encryptedHex + "\n");
//            });
//        }
//    }
//
//    // Hàm điều chỉnh key về đúng 8 bytes
//    private byte[] adjustKeyLength(byte[] keyBytes, int requiredLength) {
//        if (keyBytes.length == requiredLength) {
//            return keyBytes;
//        } else if (keyBytes.length > requiredLength) {
//            return Arrays.copyOf(keyBytes, requiredLength); // Cắt bớt
//        } else {
//            byte[] adjustedKey = new byte[requiredLength];
//            System.arraycopy(keyBytes, 0, adjustedKey, 0, keyBytes.length);
//            Arrays.fill(adjustedKey, keyBytes.length, requiredLength, (byte) 0); // Bổ sung byte 0 nếu thiếu
//            return adjustedKey;
//        }
//    }

    private void decryptMessage(String encryptedHex, String decryptionKey) {
        try {
            // Chuyển từ HEX -> byte[]
            byte[] encryptedBytes = hexToBytes(encryptedHex);
            byte[] keyBytes = decryptionKey.getBytes(StandardCharsets.UTF_8);

            // Đảm bảo key có đúng 8 bytes
            keyBytes = adjustKeyLength(keyBytes, 8);

            // Giải mã
            Des des = new Des();
            byte[] decryptedBytes = des.decrypt(encryptedBytes, keyBytes);
            String decryptedMessage = new String(decryptedBytes, StandardCharsets.UTF_8);
            if (!isValidUTF8(decryptedBytes)) {
                throw new Exception("Key Invalid Input Error");
            }
            Platform.runLater(() -> receivedMessageArea.appendText("Decrypted Message: " + decryptedMessage + "\n"));
        } catch (Exception e) {
            Platform.runLater(() -> {
                receivedMessageArea.appendText("Decryption failed: " + e.getMessage() + "\n");
                receivedMessageArea.appendText("Encrypted HEX: " + encryptedHex + "\n");
            });
        }
    }

    // ✅ Hàm kiểm tra dữ liệu có phải là UTF-8 hợp lệ không
    private boolean isValidUTF8(byte[] data) {
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            decoder.decode(ByteBuffer.wrap(data));
            return true;
        } catch (CharacterCodingException e) {
            return false; // Dữ liệu không phải UTF-8 hợp lệ
        }
    }

        private byte[] adjustKeyLength(byte[] keyBytes, int requiredLength) {
            if (keyBytes.length == requiredLength) {
                return keyBytes;
            } else if (keyBytes.length > requiredLength) {
                return Arrays.copyOf(keyBytes, requiredLength); // Cắt bớt
            } else {
                byte[] adjustedKey = new byte[requiredLength];
                System.arraycopy(keyBytes, 0, adjustedKey, 0, keyBytes.length);
                Arrays.fill(adjustedKey, keyBytes.length, requiredLength, (byte) 0); // Bổ sung byte 0 nếu thiếu
                return adjustedKey;
            }
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
