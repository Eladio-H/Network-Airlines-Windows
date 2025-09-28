package com.example.networkairlines;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class ChangeScene {
    /**
     * This function is used to change the scene in
     * the GUI to another screen. It needs the name of
     * the FXML file containing all the elements of the
     * next screen.
     * @param pane a certain screen object
     * @param fxml file of the next screen
     * @throws IOException if fxml file does not exist (though in my code it always will)
     */
    public static void change(Pane pane, String fxml) throws IOException {
        //Loads new scene from FXML file
        Stage stage = (Stage) pane.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(ChangeScene.class.getResource(fxml)));
        Parent newScene = loader.load();
        //Save the controller objects but only for some screens
        if (loader.getController() instanceof ConnectedController){
            ConnectedController.setController(loader.getController());
        } else if (loader.getController() instanceof SendDialog) {
            SendDialog.setController(loader.getController());
        }
        //Sets screen and its basic properties
        stage.setScene(new Scene(newScene));
        stage.centerOnScreen();
        stage.setOnCloseRequest(windowEvent -> System.exit(0));
    }
}