import com.normdevstorm.encryptedfiletransfer.crypto.Des;

import static com.normdevstorm.encryptedfiletransfer.crypto.Des.byteArrayToHexString;

public class TestDesAlter {
    public static void main(String[] args) {
        try {
            // Create a test key and plaintext
            String keyStr = "efsewewrwer";
            String plaintext = "   Hello DESfasjfhkjasfkjahsdfkjhaskjfhkjasdbfnmbksdfkjsdf!    ";

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
            StringBuilder encryptedHex = byteArrayToHexString(encryptedBytes);
            System.out.println("Encrypted (hex): " + encryptedHex);

            // Decrypt
            byte[] decryptedBytes = des.encrypt(encryptedBytes, keyBytes, true);
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
