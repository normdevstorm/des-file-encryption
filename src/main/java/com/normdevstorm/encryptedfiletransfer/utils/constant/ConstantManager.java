package com.normdevstorm.encryptedfiletransfer.utils.constant;

public class ConstantManager {
    public static int CHUNK_SIZE = 8192;
    public static int RETRY_LIMIT = 3;
    public static int TIMEOUT = 3;
    public static final int FILE_TRANSFER_PORT = 5000; // Your existing port
    public static final int SIGNALING_PORT = 5001;     // New port for signaling
    public static final int MESSAGING_PORT = 5050;     // New port for signaling
//"192.168.1.29"
//    public static String serverIpAddress = "192.168.1.55";
//        public static String clientIpAddress = "192.168.1.46";

    public static String serverIpAddress = "localhost";
    public static String clientIpAddress = "localhost";
    }
