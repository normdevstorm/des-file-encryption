package com.normdevstorm.encryptedfiletransfer.model;
import javafx.fxml.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.ServerSocket;

abstract public  class GenericUIController {

        @FXML public Label titleLabel;
        @FXML public Button selectFileBtn;
        @FXML public TextArea statusArea;
        abstract public void eventHandlers();
}
