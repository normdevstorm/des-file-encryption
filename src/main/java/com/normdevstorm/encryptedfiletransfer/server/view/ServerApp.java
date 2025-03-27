package com.normdevstorm.encryptedfiletransfer.server.view;

import com.normdevstorm.encryptedfiletransfer.server.controller.ServerController;
import com.normdevstorm.encryptedfiletransfer.utils.constant.ConstantManager;
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

        ServerSocket serverSocket = new ServerSocket(ConstantManager.FILE_TRANSFER_PORT);
        //Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/normdevstorm/encryptedfiletransfer/server.fxml"));
        Parent root = (Parent) fxmlLoader.load();
//            Scene scene = new Scene(root, 400, 600);
        Scene scene = new Scene(root, 450, 700);
        stage.setScene(scene);
        stage.setResizable(false); // Không cho phép thay đổi kích thước cửa sổ
        stage.show();


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