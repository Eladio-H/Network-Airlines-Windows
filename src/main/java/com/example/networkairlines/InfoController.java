package com.example.networkairlines;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class InfoController {
    @FXML
    Pane screen;

    /**
     * Runs when pressing 'Back'. Takes user back to start screen.
     * @throws IOException
     */
    @FXML
    protected void back() throws IOException {
        ChangeScene.change(screen, "start.fxml");
    }
}