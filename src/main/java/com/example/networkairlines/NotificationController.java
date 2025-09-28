package com.example.networkairlines;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class NotificationController {
    private Class<?> source;
    public void setSource(Class<?> o) {
        this.source = o;
    }
    @FXML
    private Label text;

    /**
     * Displays the notification text
     * @param text String of notification text
     */
    public void setText(String text) {
        this.text.setText(text);
    }

    /**
     * Handles the closing of the notification (clicking the 'Close' button)
     * Depending on what the 'source' Class of this notification is, the closing of the notification will trigger different actions.
     * @param actionEvent
     * @throws IOException
     */
    @FXML
    protected void onCloseButtonClick(javafx.event.ActionEvent actionEvent) throws IOException {
        //Closes the FX stage of the notification
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        stage.hide();
        //Acts differently according to the source class
        if (source == StartController.class) {
            StartController.getController().getScreen().setDisable(false);
        } else if (source == ConnectionsManager.class) {
            if (ConnectedController.getScreenIsOpen()) {
                ChangeScene.change(ConnectedController.getController().getScreen(), "start.fxml");
                ConnectedController.setScreenIsOpen(false);
            } else {
                ChangeScene.change(SendDialog.getController().getScreen(), "start.fxml");
            }
        } else if (source == SendDialog.class) {
            ChangeScene.change(SendDialog.getController().getScreen(), "connected.fxml");
        }
    }
}