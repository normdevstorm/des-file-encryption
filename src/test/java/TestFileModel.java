import com.normdevstorm.encryptedfiletransfer.model.FileMetadata;

import java.time.LocalDateTime;

public class TestFileModel {
    public static void main(String[] args) {
        FileMetadata fileMetadata = new FileMetadata("A text file",  Long.parseLong("56"), LocalDateTime.now());
        System.out.println(FileMetadata.toModel(fileMetadata.toString()));
    }
}
