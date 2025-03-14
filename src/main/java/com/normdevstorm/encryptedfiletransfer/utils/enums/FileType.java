package com.normdevstorm.encryptedfiletransfer.utils.enums;

public enum FileType {
    TEXT, IMAGE, PDF;

    static public FileType getTypeFromExtension(String extension){
        FileType type;
        switch (extension){
            case "png":
                type = IMAGE;
                break;
            case "txt":
            default:
                type = TEXT;
                break;
        }
        return type;
    }
}
