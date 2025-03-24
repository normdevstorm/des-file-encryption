module com.normdevstorm.encryptedfiletransfer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.normdevstorm.encryptedfiletransfer to javafx.fxml;
    exports com.normdevstorm.encryptedfiletransfer.model;
    exports com.normdevstorm.encryptedfiletransfer.client.view;
    opens com.normdevstorm.encryptedfiletransfer.client.view to javafx.fxml;
    exports com.normdevstorm.encryptedfiletransfer.client.controller;
    opens com.normdevstorm.encryptedfiletransfer.client.controller to javafx.fxml;
    exports com.normdevstorm.encryptedfiletransfer.server.view;
    opens com.normdevstorm.encryptedfiletransfer.server.view to javafx.fxml;
    exports com.normdevstorm.encryptedfiletransfer.server.controller;
    opens com.normdevstorm.encryptedfiletransfer.server.controller to javafx.fxml;
}