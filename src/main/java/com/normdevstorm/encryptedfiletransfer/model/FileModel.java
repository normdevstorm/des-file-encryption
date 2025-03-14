package com.normdevstorm.encryptedfiletransfer.model;

import java.io.File;
import java.time.LocalDateTime;

public class FileModel {
    private String file_name;
    private String extension;
    private Long size;
    private LocalDateTime time_sent;
    private String content;

    public FileModel(String file_name, String extension, Long size, LocalDateTime time_sent, String content) {
        this.file_name = file_name;
        this.extension = extension;
        this.size = size;
        this.time_sent = time_sent;
        this.content = content;
   }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "FileModel{" +
                "file_name='" + file_name + '\'' +
                ", extension='" + extension + '\'' +
                ", size='" + size + '\'' +
                ", time_sent='" + time_sent + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

     static public FileModel toModel(String string){
         // Remove the "FileModel{" prefix and "}" suffix
         string = string.substring(10, string.length() - 1);

         // Split the string by the field delimiters
         String[] parts = string.split(", ");

         // Extract individual field values
         String fileName = parts[0].substring(parts[0].indexOf('\'') + 1, parts[0].lastIndexOf('\''));
         String extension = parts[1].substring(parts[1].indexOf('\'') + 1, parts[1].lastIndexOf('\''));
         Long size = Long.parseLong(parts[2].substring(parts[2].indexOf('\'') + 1, parts[2].lastIndexOf('\'')));
         LocalDateTime timeSent = LocalDateTime.parse(parts[3].substring(parts[3].indexOf('\'') + 1, parts[3].lastIndexOf('\'')));

         // Extract content (the rest of the string after "content='")
         String content = parts[4].substring(parts[4].indexOf('\'') + 1, parts[4].lastIndexOf('\''));

         // Create and return a new FileModel object
         return new FileModel(fileName, extension, size, timeSent, content);
    }
}
