package com.example.networkairlines;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Start extends Application {
    /**
     * Function that opens the interactive screen by loading
     * the FXML files and controllers.
     * @param stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Start.class.getResource("start.fxml"));
        Scene scene = new Scene(loader.load(), 550, 700);
        StartController.setController(loader.getController());
        stage.setResizable(false);
        stage.setTitle("Network Airlines");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Runs at the start of the program and launches the start()
     * function which starts the graphical user interface.
     * @param args
     */
    public static void main(String[] args) {
        launch();
    }
}