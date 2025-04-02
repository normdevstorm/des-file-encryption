package com.normdevstorm.encryptedfiletransfer.crypto;

import java.util.Random;

/**
 * A simple benchmark class to measure the performance of the DES encryption algorithm.
 */
public class DesBenchmark {

    public static void main(String[] args) {
        // Create test data of various sizes
        int[] dataSizes = {1024, 10 * 1024, 100 * 1024, 1024 * 1024, 10 * 1024 * 1024};
        
        // Create a key
        byte[] key = new byte[8];
        new Random().nextBytes(key);
        
        System.out.println("DES Encryption Benchmark");
        System.out.println("=======================");
        System.out.println("Data Size (Bytes) | Encryption Time (ms) | Throughput (MB/s)");
        System.out.println("------------------|---------------------|------------------");
        
        for (int size : dataSizes) {
            // Generate random data
            byte[] data = new byte[size];
            new Random().nextBytes(data);
            
            // Create a new DES instance for each test
            Des des = new Des();
            
            // Warm-up run
            des.encrypt(data, key, false);
            
            // Measure performance
            long startTime = System.currentTimeMillis();
            byte[] encrypted = des.encrypt(data, key, false);
            long endTime = System.currentTimeMillis();
            
            long elapsedTime = endTime - startTime;
            double throughput = (size / 1024.0 / 1024.0) / (elapsedTime / 1000.0);
            
            System.out.printf("%-18d | %-21d | %.2f%n", size, elapsedTime, throughput);
            
            // Verify correctness by decrypting
            byte[] decrypted = des.encrypt(encrypted, key, true);
            boolean correct = verifyResults(data, decrypted);
            if (!correct) {
                System.out.println("  ⚠️ Warning: Decryption did not match original data for size " + size);
            }
        }
    }
    
    private static boolean verifyResults(byte[] original, byte[] decrypted) {
        // The decrypted data might be shorter than the original due to padding removal
        if (decrypted.length != original.length) {
            return false;
        }
        
        for (int i = 0; i < original.length; i++) {
            if (original[i] != decrypted[i]) {
                return false;
            }
        }
        
        return true;
    }
} 