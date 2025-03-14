import com.normdevstorm.encryptedfiletransfer.model.FileModel;

import java.time.LocalDateTime;

public class TestFileModel {
    public static void main(String[] args) {
        FileModel fileModel = new FileModel("A text file", ".txt", Long.parseLong("56"), LocalDateTime.now(), "A hihi");
        System.out.println(FileModel.toModel(fileModel.toString()));
    }
}
