package com.normdevstorm.encryptedfiletransfer.crypto;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

public class Des {
    // DES algorithm tables
    private static final int[] IP = {
            58, 50, 42, 34, 26, 18, 10, 2,
            60, 52, 44, 36, 28, 20, 12, 4,
            62, 54, 46, 38, 30, 22, 14, 6,
            64, 56, 48, 40, 32, 24, 16, 8,
            57, 49, 41, 33, 25, 17, 9, 1,
            59, 51, 43, 35, 27, 19, 11, 3,
            61, 53, 45, 37, 29, 21, 13, 5,
            63, 55, 47, 39, 31, 23, 15, 7
    };

    private static final int[] IP2 = {
            40, 8, 48, 16, 56, 24, 64, 32,
            39, 7, 47, 15, 55, 23, 63, 31,
            38, 6, 46, 14, 54, 22, 62, 30,
            37, 5, 45, 13, 53, 21, 61, 29,
            36, 4, 44, 12, 52, 20, 60, 28,
            35, 3, 43, 11, 51, 19, 59, 27,
            34, 2, 42, 10, 50, 18, 58, 26,
            33, 1, 41, 9, 49, 17, 57, 25
    };

    private static final int[] E = {
            32, 1, 2, 3, 4, 5,
            4, 5, 6, 7, 8, 9,
            8, 9, 10, 11, 12, 13,
            12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29,
            28, 29, 30, 31, 32, 1
    };

    private static final int[] P = {
            16, 7, 20, 21, 29, 12, 28, 17,
            1, 15, 23, 26, 5, 18, 31, 10,
            2, 8, 24, 14, 32, 27, 3, 9,
            19, 13, 30, 6, 22, 11, 4, 25
    };

    private static final int[] PC1 = {
            57, 49, 41, 33, 25, 17, 9,
            1, 58, 50, 42, 34, 26, 18,
            10, 2, 59, 51, 43, 35, 27,
            19, 11, 3, 60, 52, 44, 36,
            63, 55, 47, 39, 31, 23, 15,
            7, 62, 54, 46, 38, 30, 22,
            14, 6, 61, 53, 45, 37, 29,
            21, 13, 5, 28, 20, 12, 4
    };

    private static final int[] PC2 = {
            14, 17, 11, 24, 1, 5, 3, 28,
            15, 6, 21, 10, 23, 19, 12, 4,
            26, 8, 16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55, 30, 40,
            51, 45, 33, 48, 44, 49, 39, 56,
            34, 53, 46, 42, 50, 36, 29, 32
    };

    private static final int[][] S = {
            // S1
            {
                    14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
                    0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
                    4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
                    15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13
            },
            // S2
            {
                    15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
                    3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
                    0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
                    13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9
            },
            // S3
            {
                    10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
                    13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
                    13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
                    1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12
            },
            // S4
            {
                    7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
                    13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
                    10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
                    3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14
            },
            // S5
            {
                    2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
                    14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
                    4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
                    11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3
            },
            // S6
            {
                    12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
                    10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
                    9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
                    4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13
            },
            // S7
            {
                    4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
                    13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
                    1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
                    6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12
            },
            // S8
            {
                    13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
                    1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
                    7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
                    2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11
            }
    };

    // Precomputed lookup tables for bit operations
    private static final byte[] BIT_MASK = {(byte)0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
    private static final int[] BIT_SHIFT = {7, 6, 5, 4, 3, 2, 1, 0};
    
    // Optimal batch size for block processing (experimentally determined)
    private static final int OPTIMAL_BATCH_SIZE = 256; // Smaller batch size for better load balancing
    
    // Thread pool for parallel processing
    private static ExecutorService threadPool;
    
    static {
        // Initialize thread pool with optimal size based on available processors
        int processors = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(processors * 2);
        
        // Register shutdown hook to ensure thread pool is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }));
    }
    
    // Precomputed S-box lookup tables (direct indexed)
    private static final int[][][] SBOX_LOOKUP = new int[8][4][16];
    
    static {
        // Initialize the S-box lookup tables for direct indexing
        for (int box = 0; box < 8; box++) {
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 16; col++) {
                    SBOX_LOOKUP[box][row][col] = S[box][col + row * 16];
                }
            }
        }
    }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public byte[] encrypt(byte[] text, byte[] keyMaterial, boolean decrypt) {
        // Ensure text length is multiple of 8 (DES block size)
        int originalLength = text.length;
        int paddedLength = (originalLength + 7) / 8 * 8; // Round up to multiple of 8

        byte[] paddedText = Arrays.copyOf(text, paddedLength);
        byte[][] subKeys = generateSubkeys(keyMaterial);
        if (decrypt) {
            // Reverse the order of subkeys for decryption
            for (int i = 0; i < subKeys.length / 2; i++) {
                byte[] temp = subKeys[i];
                subKeys[i] = subKeys[subKeys.length - 1 - i];
                subKeys[subKeys.length - 1 - i] = temp;
            }
        }

        int blockCount = paddedText.length / 8;
        final byte[] result = new byte[paddedLength];

        // Process in batches for better scheduling and less overhead
        try {
            List<Future<?>> futures = new ArrayList<>();
            
            // Process blocks in batches with safeguards
            for (int startBlock = 0; startBlock < blockCount; startBlock += OPTIMAL_BATCH_SIZE) {
                final int finalStartBlock = startBlock;
                final int endBlock = Math.min(startBlock + OPTIMAL_BATCH_SIZE, blockCount);
                futures.add(threadPool.submit(() -> {
                    try {
                        for (int blockIndex = finalStartBlock; blockIndex < endBlock; blockIndex++) {
                            if (blockIndex * 8 + 8 <= paddedText.length) {
                                byte[] block = new byte[8];
                                System.arraycopy(paddedText, blockIndex * 8, block, 0, 8);
                                byte[] encryptedBlock = processBlockOptimized(block, subKeys);
                                System.arraycopy(encryptedBlock, 0, result, blockIndex * 8, 8);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error processing block", e);
                    }
                }));
            }
            
            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Encryption interrupted", e);
        }

        // If decrypting, return original length
        if (decrypt) {
            return removeZeroPadding(result);
        }

        return result;
    }

    // Optimized block processing method
    private byte[] processBlockOptimized(byte[] block, byte[][] subKeys) {
        // Initial permutation - optimized version
        byte[] output = useTableOptimized(block, IP);
        
        // Working with individual integers for L and R instead of byte arrays
        // to minimize array operations and memory allocation
        int L = ((output[0] & 0xFF) << 24) | ((output[1] & 0xFF) << 16) | 
                ((output[2] & 0xFF) << 8) | (output[3] & 0xFF);
        int R = ((output[4] & 0xFF) << 24) | ((output[5] & 0xFF) << 16) | 
                ((output[6] & 0xFF) << 8) | (output[7] & 0xFF);
        
        // 16 rounds of processing
        for (int stage = 0; stage < 16; stage++) {
            int temp = R;
            
            // Apply Feistel function
            // Expansion, XOR with key, S-box, and permutation combined in one step
            R = L ^ feistelFunction(R, subKeys[stage]);
            
            L = temp;
        }
        
        // Swap L and R for the final round
        int temp = L;
        L = R;
        R = temp;
        
        // Convert back to bytes
        output[0] = (byte)(L >>> 24);
        output[1] = (byte)(L >>> 16);
        output[2] = (byte)(L >>> 8);
        output[3] = (byte)L;
        output[4] = (byte)(R >>> 24);
        output[5] = (byte)(R >>> 16);
        output[6] = (byte)(R >>> 8);
        output[7] = (byte)R;
        
        // Final permutation
        return useTableOptimized(output, IP2);
    }
    
    // Fast Feistel function implementation
    private int feistelFunction(int r, byte[] subkey) {
        // Expansion - expand right half from 32 to 48 bits according to E table
        long expanded = 0;
        for (int i = 0; i < E.length; i++) {
            if ((r & (1 << (32 - E[i]))) != 0) {
                expanded |= (1L << (47 - i));
            }
        }
        
        // XOR with subkey (48 bits)
        long keyValue = 0;
        for (int i = 0; i < 6 && i < subkey.length; i++) {
            keyValue |= ((subkey[i] & 0xFFL) << (8 * (5 - i)));
        }
        expanded ^= keyValue;
        
        // Apply S-box substitution (optimized to work on bits directly)
        int sOutput = 0;
        for (int box = 0; box < 8; box++) {
            int position = box * 6;
            // Extract 6 bits for current S-box (one byte from expanded 48-bit value)
            int sixBits = (int)((expanded >> (42 - position)) & 0x3F);
            
            // Calculate row (first and last bit)
            int row = ((sixBits & 0x20) >> 4) | (sixBits & 0x01);
            
            // Calculate column (middle 4 bits)
            int col = (sixBits >> 1) & 0x0F;
            
            // Get S-box output value (4 bits)
            int value = SBOX_LOOKUP[box][row][col];
            
            // Insert 4-bit output value into appropriate position in 32-bit output
            sOutput |= (value << (28 - box * 4));
        }
        
        // Apply permutation P - with bounds checking
        int result = 0;
        for (int i = 0; i < P.length; i++) {
            int bitPosition = P[i] - 1; // P values are 1-indexed
            if (bitPosition >= 0 && bitPosition < 32) {
                if ((sOutput & (1 << (31 - bitPosition))) != 0) {
                    result |= (1 << (31 - i));
                }
            }
        }
        
        return result;
    }
    
    // Optimized table lookup method that reduces memory allocations
    private byte[] useTableOptimized(byte[] input, int[] table) {
        int len = table.length;
        int byteNum = (len - 1) / 8 + 1;
        byte[] output = new byte[byteNum];
        
        for (int i = 0; i < len; i++) {
            int sourcePos = table[i] - 1;
            if (sourcePos < 0 || sourcePos >= input.length * 8) {
                continue; // Skip invalid source position
            }
            
            int sourceByte = sourcePos / 8;
            int sourceBit = 7 - (sourcePos % 8);
            
            int targetByte = i / 8;
            int targetBit = 7 - (i % 8);
            
            if (sourceByte < input.length && targetByte < output.length) {
                if ((input[sourceByte] & (1 << sourceBit)) != 0) {
                    output[targetByte] |= (1 << targetBit);
                }
            }
        }
        
        return output;
    }

    private static byte[] removeZeroPadding(byte[] data) {
        // Find last non-zero byte
        int i = data.length - 1;
        while (i >= 0 && data[i] == 0) {
            i--;
        }

        // Return array without trailing zeros
        return Arrays.copyOf(data, i + 1);
    }

    public byte[] encryptText(byte[] text, byte[] keyMaterial, boolean decrypt) {
        byte[][] subKeys = generateSubkeys(keyMaterial);
        if (decrypt) {
            // Reverse the order of subkeys for decryption
            for (int i = 0; i < subKeys.length / 2; i++) {
                byte[] temp = subKeys[i];
                subKeys[i] = subKeys[subKeys.length - 1 - i];
                subKeys[subKeys.length - 1 - i] = temp;
            }
        }

        return processText(text, subKeys);
    }

    private byte[] processText(byte[] textbytes, byte[][] subkeyarray) {
        int blockCount = textbytes.length / 8;
        byte[] tmp = new byte[8];

        for (int blocknum = 0; blocknum < blockCount; blocknum++) {
            // Initial permutation
            System.arraycopy(textbytes, blocknum * 8, tmp, 0, 8);
            tmp = useTable(tmp, IP);
            System.arraycopy(tmp, 0, textbytes, blocknum * 8, 8);

            byte[] holderL = new byte[4];
            byte[] holderR = new byte[4];

            // Split into left and right parts
            System.arraycopy(textbytes, blocknum * 8, holderL, 0, 4);
            System.arraycopy(textbytes, blocknum * 8 + 4, holderR, 0, 4);

            // 16 rounds of processing
            for (int stage = 1; stage <= 16; stage++) {
                byte[] oldR = Arrays.copyOf(holderR, holderR.length);

                // Expansion E
                byte[] expandedR = useTable(holderR, E);
                // XOR with the subkey
                for (int i = 0; i < expandedR.length; i++) {
                    expandedR[i] ^= subkeyarray[stage - 1][i];
                }

                // S-box substitution
                byte[] sOutput = sbox(expandedR);

                // Permutation P
                sOutput = useTable(sOutput, P);

                // XOR with left side and swap
                for (int i = 0; i < holderL.length; i++) {
                    holderR[i] = (byte) (holderL[i] ^ sOutput[i]);
                }

                holderL = oldR;

                // Special handling after the 16th round
                for (int pointer = 0; pointer < 4; pointer++) {
                    textbytes[pointer + blocknum * 8] = holderL[pointer];
                    textbytes[pointer + blocknum * 8 + 4] = holderR[pointer];
                    if (stage == 16) {
                        textbytes[pointer + blocknum * 8] = holderR[pointer];
                        textbytes[pointer + blocknum * 8 + 4] = holderL[pointer];
                    }
                }

                if (stage == 16) {
                    System.arraycopy(textbytes, blocknum * 8, tmp, 0, 8);
                    tmp = useTable(tmp, IP2);
                    System.arraycopy(tmp, 0, textbytes, blocknum * 8, 8);
                }
            }
        }

        return textbytes;
    }

    private byte[] useTable(byte[] arr, int[] table) {
        int len = table.length;
        int byteNum = (len - 1) / 8 + 1;
        byte[] output = new byte[byteNum];

        for (int i = 0; i < len; i++) {
            int val = getBitAt(arr, table[i] - 1);
            setBitAt(output, i, val);
        }

        return output;
    }

    private byte[] glueKey(byte[] arrC, byte[] arrD) {
        byte[] result = new byte[7];

        // Copy first 3 bytes = 24 bits of arrC
        System.arraycopy(arrC, 0, result, 0, 3);

        // Copy bits 24-27 from arrC to result
        for (int i = 0; i < 4; i++) {
            int val = getBitAt(arrC, 24 + i);
            setBitAt(result, 24 + i, val);
        }

        // Copy all 28 bits from arrD to result (positions 28-55)
        for (int i = 0; i < 28; i++) {
            int val = getBitAt(arrD, i);
            setBitAt(result, 28 + i, val);
        }

        return result;
    }

    private byte[] sbox(byte[] input) {
        byte[] output = new byte[4];

        // Input = 48 bits (expanded E after XOR with the key)
        // Output = 32 bits after S-box substitution
        // Process 8 sections (6 bits each)
        for (int section = 0; section < 8; section++) {
            // Get row bits (first and last bit of the section)
            int row = (getBitAt(input, section * 6) << 1) | getBitAt(input, section * 6 + 5);

            // Get column bits (middle 4 bits of the section)
            int col = 0;
            for (int colbit = 0; colbit < 4; colbit++) {
                col |= getBitAt(input, section * 6 + colbit + 1) << (3 - colbit);
            }

            // Get the S-box output (4 bits)
            byte halfOfByte = (byte) sboxPicker(col, row, section);

            // Pack the 4 bits into the output bytes
            if ((section & 1) == 0) {
                output[section / 2] |= (byte) (halfOfByte << 4);
            } else {
                output[section / 2] |= halfOfByte;
            }
        }

        return output;
    }

    private static int sboxPicker(int col, int row, int index) {
        // S-box lookup based on the section index
        return S[index][col + row * 16];
    }

    private static int getBitAt(byte[] data, int pos) {
        int bytePos = pos / 8;
        int bitPos = pos % 8;
        return (data[bytePos] >> (7 - bitPos)) & 1;
    }

    private static void setBitAt(byte[] data, int pos, int val) {
        int bytePos = pos / 8;
        int bitPos = pos % 8;
        if (val == 1) {
            data[bytePos] |= (1 << (7 - bitPos));
        } else {
            data[bytePos] &= ~(1 << (7 - bitPos));
        }
    }

    private byte[][] generateSubkeys(byte[] keyBytes) {
        byte[][] subKeyArray = new byte[16][];
        keyBytes = useTable(keyBytes, PC1); // Reduces to 56 bits

        byte[] keyBytesC = selectBits(keyBytes, 0, 28);
        byte[] keyBytesD = selectBits(keyBytes, 28, 28);

        for (int i = 1; i <= 16; i++) {
            // Apply rotation schedule according to DES standard
            if (i == 1 || i == 2 || i == 9 || i == 16) {
                keyBytesC = rotateLeft(keyBytesC, 28, 1);
                keyBytesD = rotateLeft(keyBytesD, 28, 1);
            } else {
                keyBytesC = rotateLeft(keyBytesC, 28, 2);
                keyBytesD = rotateLeft(keyBytesD, 28, 2);
            }

            // Combine C and D blocks, then apply PC2 permutation
            keyBytes = useTable(glueKey(keyBytesC, keyBytesD), PC2);
            subKeyArray[i - 1] = keyBytes;
        }

        return subKeyArray;
    }

    private byte[] selectBits(byte[] data, int startBit, int length) {
        int resultSize = (length - 1) / 8 + 1;
        byte[] result = new byte[resultSize];

        for (int i = 0; i < length; i++) {
            int val = getBitAt(data, startBit + i);
            setBitAt(result, i, val);
        }

        return result;
    }

    private byte[] rotateLeft(byte[] data, int bitLength, int rotateCount) {
        byte[] result = new byte[data.length];

        for (int i = 0; i < bitLength; i++) {
            int newPos = (i + rotateCount) % bitLength;
            int val = getBitAt(data, i);
            setBitAt(result, newPos, val);
        }

        return result;
    }

    public static StringBuilder byteArrayToHexString(byte[] ba) {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba) {
            hex.append(String.format("%02x", b & 0xff));
        }
        return hex;
    }

    // Optimized version of getBitAt using lookup tables
    private static int getBitAtOptimized(byte[] data, int pos) {
        int bytePos = pos / 8;
        int bitPos = pos % 8;
        return (data[bytePos] & BIT_MASK[bitPos]) != 0 ? 1 : 0;
    }

    // Optimized version of setBitAt using lookup tables
    private static void setBitAtOptimized(byte[] data, int pos, int val) {
        int bytePos = pos / 8;
        int bitPos = pos % 8;
        if (val == 1) {
            data[bytePos] |= BIT_MASK[bitPos];
        } else {
            data[bytePos] &= ~BIT_MASK[bitPos];
        }
    }

    // Fast S-box implementation with direct indexing
    private byte[] sboxOptimized(byte[] input) {
        byte[] output = new byte[4];
        
        for (int box = 0; box < 8; box++) {
            // Extract 6 bits for this S-box
            int bits = 0;
            for (int i = 0; i < 6; i++) {
                if (getBitAtOptimized(input, box * 6 + i) == 1) {
                    bits |= (1 << (5 - i));
                }
            }
            
            // Calculate row and column
            int row = ((bits & 0x20) >> 4) | (bits & 0x01);
            int col = (bits >> 1) & 0x0F;
            
            // Get value from precomputed lookup table
            int value = SBOX_LOOKUP[box][row][col];
            
            // Pack into output
            if (box % 2 == 0) {
                output[box / 2] |= (value << 4);
            } else {
                output[box / 2] |= value;
            }
        }
        
        return output;
    }
}
