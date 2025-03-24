package com.normdevstorm.encryptedfiletransfer.client.view;

import com.normdevstorm.encryptedfiletransfer.client.controller.ClientController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {


        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText("Client đã khởi động thành công!");
            alert.setContentText("Client đang lắng nghe trên cổng 5050.");
            alert.showAndWait();
        });


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/normdevstorm/encryptedfiletransfer/client.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        Scene scene = new Scene(root, 400, 800);
        scene.getStylesheets().add(getClass().getResource("/com/normdevstorm/encryptedfiletransfer/styles.css").toExternalForm());

        ClientController clientController = fxmlLoader.getController();
        clientController.initializeController();

        stage.setScene(scene);
        stage.setTitle("File transfer client");
        stage.show();
    }
}
