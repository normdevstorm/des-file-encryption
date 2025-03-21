package com.normdevstorm.encryptedfiletransfer.model;

import java.time.LocalDateTime;

public class FileMetadata {
    private String file_name;
    private Long size;
    private LocalDateTime time_sent;

    public FileMetadata(){

    }

    public FileMetadata(String file_name, Long size, LocalDateTime time_sent) {
        this.file_name = file_name;
        this.size = size;
        this.time_sent = time_sent;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }


    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDateTime getTime_sent() {
        return time_sent;
    }

    public void setTime_sent(LocalDateTime time_sent) {
        this.time_sent = time_sent;
    }

    @Override
    public String toString() {
        return "FileModel{" + "file_name='" + file_name + '\'' + ", size='" + size + '\'' + ", time_sent='" + time_sent + '\'' + '}';
    }

    static public FileMetadata toModel(String string) {
        // Extract values from the string representation
        int fileNameStart = string.indexOf("file_name='") + 11;
        int fileNameEnd = string.indexOf("'", fileNameStart);
        String fileName = string.substring(fileNameStart, fileNameEnd);

        int sizeStart = string.indexOf("size='") + 6;
        int sizeEnd = string.indexOf("'", sizeStart);
        Long size = Long.parseLong(string.substring(sizeStart, sizeEnd));

        int timeStart = string.indexOf("time_sent='") + 11;
        int timeEnd = string.indexOf("'", timeStart);
        LocalDateTime timeSent = LocalDateTime.parse(string.substring(timeStart, timeEnd));

        return new FileMetadata(fileName, size, timeSent);
    }
}
