import com.normdevstorm.encryptedfiletransfer.crypto.Des;

import java.util.Arrays;

import static com.normdevstorm.encryptedfiletransfer.crypto.Des.byteArrayToHexString;
import static com.normdevstorm.encryptedfiletransfer.crypto.Des.hexStringToByteArray;

public class TestDesAlter {
    public static void main(String[] args) {
        try {
            // Create a test key and plaintext
            String keyStr = "1234567ertgdfgd8";
            String plaintext = "     package com.normdevstorm.encryptedfiletransfer.utils.constant;\n" +
                    "\n" +
                    "public class ConstantManager {\n" +
                    "    public static int CHUNK_SIZE = 8192;\n" +
                    "    public static int RETRY_LIMIT = 3;\n" +
                    "    public static int TIMEOUT = 3;\n" +
                    "    public static final int FILE_TRANSFER_PORT = 5000; // Your existing port\n" +
                    "    public static final int SIGNALING_PORT = 5001;     // New port for signaling\n" +
                    "    public static final int MESSAGING_PORT = 5050;     // New port for signaling\n" +
                    "//\"192.168.1.29\"\n" +
                    "    public static String serverIpAddress = \"localhost\";\n" +
                    "        public static String clientIpAddress = \"localhost\"; 32485uyqweihtr89734t qguhierfghiuewr";

            System.out.println("Original text: " + plaintext);

            // Convert key and plaintext to byte arrays
            byte[] keyBytes = keyStr.getBytes();
            byte[] plaintextBytes = plaintext.getBytes();

            // Pad the plaintext to a multiple of 8 bytes if needed
/*
            if (plaintextBytes.length % 8 != 0) {
                byte[] paddedPlaintext = new byte[plaintextBytes.length + (8 - (plaintextBytes.length % 8))];
                System.arraycopy(plaintextBytes, 0, paddedPlaintext, 0, plaintextBytes.length);
                // Fill remaining bytes with spaces
                Arrays.fill(paddedPlaintext, plaintextBytes.length, paddedPlaintext.length, (byte)' ');
                plaintextBytes = paddedPlaintext;
            }
*/

            Des des = new Des();

            // Encrypt
            byte[] encryptedBytes = des.encrypt(plaintextBytes, keyBytes, false);
            System.out.println(Arrays.toString(plaintextBytes));
            StringBuilder encryptedHex = byteArrayToHexString(encryptedBytes);
//            String encryptedString = Arrays.toString(encryptedBytes);

            // Decrypt
//            System.out.println("Encrypted Bytes");
//            byte[] byteFromString = hexStringToByteArray(encryptedHex.toString());
            byte[] decryptedBytes = des.encrypt(encryptedBytes, keyBytes, true);
            System.out.println(Arrays.toString(decryptedBytes));
            String decryptedText = new String(decryptedBytes);
            System.out.println("Decrypted text: " + decryptedText);

            // Validate result
            System.out.println("Decryption successful: " +
                    plaintext.equals(decryptedText.substring(0, plaintext.length())));

        } catch (Exception e) {
            System.out.println("Error during encryption/decryption test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
