package com.normdevstorm.encryptedfiletransfer.server.view;
import com.normdevstorm.encryptedfiletransfer.server.controller.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;

public class ServerApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        ServerSocket serverSocket = new ServerSocket(5000);
            /// TODO: add the noti dialog here


            //Load FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/normdevstorm/encryptedfiletransfer/server.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Scene scene = new Scene(root, 400, 500);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/normdevstorm/encryptedfiletransfer/styles.css")).toExternalForm());
            // Add controller
            ServerController serverController = fxmlLoader.getController();
            serverController.initializeController(serverSocket, stage);

            // Config stage
            stage.setTitle("File transfer server");
            stage.setScene(scene);
            stage.show();

        }
    public static void main(String[] args) {
        launch(args);
    }
}