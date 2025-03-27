package com.normdevstorm.encryptedfiletransfer.client.view;

import com.normdevstorm.encryptedfiletransfer.client.controller.ClientController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/normdevstorm/encryptedfiletransfer/client.fxml"));
        Parent root = (Parent) fxmlLoader.load();
//        Scene scene = new Scene(root, 400, 600);
        Scene scene = new Scene(root, 450, 700);
        stage.setScene(scene);
        stage.setResizable(false); // Không cho phép thay đổi kích thước cửa sổ
        stage.show();
        scene.getStylesheets().add(getClass().getResource("/com/normdevstorm/encryptedfiletransfer/styles.css").toExternalForm());

        ClientController clientController = fxmlLoader.getController();
        clientController.initializeController();

        stage.setScene(scene);
        stage.setTitle("File transfer client");
        stage.show();
    }
}
