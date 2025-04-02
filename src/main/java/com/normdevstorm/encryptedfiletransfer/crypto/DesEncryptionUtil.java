package com.normdevstorm.encryptedfiletransfer.crypto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for DES encryption operations with additional optimizations.
 */
public class DesEncryptionUtil {
    
    private static final int BUFFER_SIZE = 1024 * 1024; // 1MB buffer for file operations
    
    /**
     * Encrypt a file using DES algorithm
     *
     * @param sourceFile Path to source file
     * @param targetFile Path to target encrypted file
     * @param password Password for encryption
     * @throws Exception If encryption fails
     */
    public static void encryptFile(Path sourceFile, Path targetFile, String password) throws Exception {
        byte[] key = generateKeyFromPassword(password);
        Des des = new Des();
        
        try (InputStream in = new BufferedInputStream(Files.newInputStream(sourceFile), BUFFER_SIZE);
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetFile), BUFFER_SIZE)) {
            
            // Write the file size at the beginning for later decryption
            long fileSize = Files.size(sourceFile);
            out.write(longToBytes(fileSize));
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                if (bytesRead < buffer.length) {
                    // Last chunk might need resizing
                    byte[] lastChunk = Arrays.copyOf(buffer, bytesRead);
                    byte[] encrypted = des.encrypt(lastChunk, key, false);
                    out.write(encrypted);
                } else {
                    byte[] encrypted = des.encrypt(buffer, key, false);
                    out.write(encrypted);
                }
            }
        }
    }
    
    /**
     * Decrypt a file using DES algorithm
     *
     * @param sourceFile Path to encrypted file
     * @param targetFile Path to target decrypted file
     * @param password Password for decryption
     * @throws Exception If decryption fails
     */
    public static void decryptFile(Path sourceFile, Path targetFile, String password) throws Exception {
        byte[] key = generateKeyFromPassword(password);
        Des des = new Des();
        
        try (InputStream in = new BufferedInputStream(Files.newInputStream(sourceFile), BUFFER_SIZE);
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetFile), BUFFER_SIZE)) {
            
            // Read the original file size stored at the beginning
            byte[] sizeBytes = new byte[8];
            in.read(sizeBytes);
            long originalSize = bytesToLong(sizeBytes);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalWritten = 0;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                if (bytesRead < buffer.length) {
                    // Last chunk might need resizing
                    byte[] lastChunk = Arrays.copyOf(buffer, bytesRead);
                    byte[] decrypted = des.encrypt(lastChunk, key, true);
                    
                    // For the last chunk, we need to ensure we don't write more than original file size
                    long remaining = originalSize - totalWritten;
                    if (remaining < decrypted.length) {
                        out.write(decrypted, 0, (int)remaining);
                    } else {
                        out.write(decrypted);
                    }
                    totalWritten += decrypted.length;
                } else {
                    byte[] decrypted = des.encrypt(buffer, key, true);
                    
                    // Ensure we don't write more than original file size
                    long remaining = originalSize - totalWritten;
                    if (remaining < decrypted.length) {
                        out.write(decrypted, 0, (int)remaining);
                        break;
                    } else {
                        out.write(decrypted);
                        totalWritten += decrypted.length;
                    }
                }
            }
        }
    }
    
    /**
     * Asynchronously encrypt a file using DES algorithm
     *
     * @param sourceFile Path to source file
     * @param targetFile Path to target encrypted file
     * @param password Password for encryption
     * @return CompletableFuture that completes when encryption finishes
     */
    public static CompletableFuture<Void> encryptFileAsync(Path sourceFile, Path targetFile, String password) {
        return CompletableFuture.runAsync(() -> {
            try {
                encryptFile(sourceFile, targetFile, password);
            } catch (Exception e) {
                throw new RuntimeException("Encryption failed", e);
            }
        });
    }
    
    /**
     * Asynchronously decrypt a file using DES algorithm
     *
     * @param sourceFile Path to encrypted file
     * @param targetFile Path to target decrypted file
     * @param password Password for decryption
     * @return CompletableFuture that completes when decryption finishes
     */
    public static CompletableFuture<Void> decryptFileAsync(Path sourceFile, Path targetFile, String password) {
        return CompletableFuture.runAsync(() -> {
            try {
                decryptFile(sourceFile, targetFile, password);
            } catch (Exception e) {
                throw new RuntimeException("Decryption failed", e);
            }
        });
    }
    
    /**
     * Encrypt a string using DES
     *
     * @param text Text to encrypt
     * @param password Password for encryption
     * @return Base64 encoded encrypted string
     */
    public static String encryptString(String text, String password) throws Exception {
        byte[] textBytes = text.getBytes();
        byte[] key = generateKeyFromPassword(password);
        
        Des des = new Des();
        byte[] encrypted = des.encrypt(textBytes, key, false);
        
        return Base64Utils.encode(encrypted);
    }
    
    /**
     * Decrypt a string using DES
     *
     * @param encryptedText Base64 encoded encrypted text
     * @param password Password for decryption
     * @return Decrypted string
     */
    public static String decryptString(String encryptedText, String password) throws Exception {
        byte[] encryptedBytes = Base64Utils.decode(encryptedText);
        byte[] key = generateKeyFromPassword(password);
        
        Des des = new Des();
        byte[] decrypted = des.encrypt(encryptedBytes, key, true);
        
        return new String(decrypted);
    }
    
    /**
     * Generate a consistent 8-byte key from a password string
     *
     * @param password The password to use
     * @return 8-byte key for DES
     */
    private static byte[] generateKeyFromPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        
        // DES needs exactly 8 bytes (64 bits) for the key
        return Arrays.copyOf(hash, 8);
    }
    
    /**
     * Convert a long value to byte array
     */
    private static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(value & 0xFF);
            value >>= 8;
        }
        return result;
    }
    
    /**
     * Convert a byte array to long value
     */
    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }
    
    /**
     * Simple Base64 utility class
     */
    private static class Base64Utils {
        private static final java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        private static final java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
        
        public static String encode(byte[] data) {
            return encoder.encodeToString(data);
        }
        
        public static byte[] decode(String base64) {
            return decoder.decode(base64);
        }
    }
} 