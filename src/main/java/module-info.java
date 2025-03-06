module com.normdevstorm.encryptedfiletransfer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.normdevstorm.encryptedfiletransfer to javafx.fxml;
    exports com.normdevstorm.encryptedfiletransfer;
}