package com.normdevstorm.encryptedfiletransfer.model;

public class KeyModel {
    private static String publicKey;
    private static String privateKey;
    private static String nModulus;

    public KeyModel() {
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        KeyModel.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        KeyModel.privateKey = privateKey;
    }

    public  String getnModulus() {
        return nModulus;
    }

    public void setnModulus(String nModulus) {
        KeyModel.nModulus = nModulus;
    }
}
