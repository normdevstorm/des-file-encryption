package com.normdevstorm.encryptedfiletransfer.crypto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        byte[][] subKeys = generateSubkeys(keyMaterial);
        if (decrypt) {
            // Reverse the order of subkeys for decryption
            for (int i = 0; i < subKeys.length / 2; i++) {
                byte[] temp = subKeys[i];
                subKeys[i] = subKeys[subKeys.length - 1 - i];
                subKeys[subKeys.length - 1 - i] = temp;
            }
        }
        int blockCount = text.length / 8;
        byte[] result = new byte[text.length];

        // Create a thread pool with the number of available processors
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // Submit tasks to the executor
        for (int blocknum = 0; blocknum < blockCount; blocknum++) {
            final int blockIndex = blocknum;
            executor.submit(() -> {
                byte[] block = new byte[8];
                System.arraycopy(text, blockIndex * 8, block, 0, 8);
                byte[] encryptedBlock = processBlock(block, subKeys);
                System.arraycopy(encryptedBlock, 0, result, blockIndex * 8, 8);
            });
        }

        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Encryption interrupted", e);
        }

        return result;
    }


    private byte[] processBlock(byte[] block, byte[][] subKeys) {
        // Your existing processText logic for a single block
        byte[] tmp = new byte[8];
        System.arraycopy(block, 0, tmp, 0, 8);
        tmp = useTable(tmp, IP);
        System.arraycopy(tmp, 0, block, 0, 8);

        byte[] holderL = new byte[4];
        byte[] holderR = new byte[4];

        System.arraycopy(block, 0, holderL, 0, 4);
        System.arraycopy(block, 4, holderR, 0, 4);

        for (int stage = 1; stage <= 16; stage++) {
            byte[] oldR = Arrays.copyOf(holderR, holderR.length);
            byte[] expandedR = useTable(holderR, E);
            for (int i = 0; i < expandedR.length; i++) {
                expandedR[i] ^= subKeys[stage - 1][i];
            }
            byte[] sOutput = sbox(expandedR);
            sOutput = useTable(sOutput, P);
            for (int i = 0; i < holderL.length; i++) {
                holderR[i] = (byte) (holderL[i] ^ sOutput[i]);
            }
            holderL = oldR;

            if (stage == 16) {
                System.arraycopy(holderR, 0, block, 0, 4);
                System.arraycopy(holderL, 0, block, 4, 4);
                tmp = useTable(block, IP2);
                System.arraycopy(tmp, 0, block, 0, 8);
            }
        }

        return block;
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

        // Copy first 3 bytes of arrC
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
}
