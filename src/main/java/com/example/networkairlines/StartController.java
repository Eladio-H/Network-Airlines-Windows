package com.example.networkairlines;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

public class StartController implements Initializable {
    private static StartController controller;
    public static StartController getController() {
        return controller;
    }
    public static void setController(StartController controller) {
        StartController.controller = controller;
    }
    public static File documents;
    private boolean firstTime;
    @FXML
    private ImageView start;
    @FXML
    private ImageView settings;
    @FXML
    private ImageView info;
    @FXML
    private VBox screen;
    public VBox getScreen() {
        return screen;
    }

    /**
     * Determines wether user is connected to the internet by testing a connection to a well-known domain.
     * @return
     */
    public static boolean isConnectedToInternet(){
        try {
            InetAddress wellKnownDomain = InetAddress.getByName("google.com");
            return !wellKnownDomain.equals("");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Runs when pressing the 'Start' button. Makes the user go 'online' by taking
     * them to the main screen and starting ping connections to establish connections
     * with other users (provided that the appropriate data is available).
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @FXML
    protected void onStartButtonClick() throws IOException, NoSuchAlgorithmException {
        //If not connected to the internet, show error.
        if (!isConnectedToInternet()) {
            FXMLLoader loader = new FXMLLoader(StartController.class.getResource("notification.fxml"));
            Scene scene = new Scene(loader.load());
            NotificationController controller = loader.getController();
            controller.setSource(StartController.class);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setOnCloseRequest(event -> {
                screen.setDisable(false);
            });
            stage.show();
            screen.setDisable(true);
        } else { //Is connected to the internet
            File thisClass = new File(Start.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            File mainFolderTemp = thisClass.getParentFile().getParentFile();
            String path = mainFolderTemp.getPath();
            while (path.contains("%20")) {
                int index = path.indexOf("%20");
                String newString = path.substring(0,index) + " " + path.substring(index+3);
                path = newString;
            }
            documents = new File(path + "/docs");
            documents.mkdirs();
            File userInfo = new File(path + "/docs/userInfo.txt");
            //If data is not available, show data input dialog
            if (firstTime) {
                userInfo.createNewFile();
                ChangeScene.change(screen,"introData.fxml");
            } else { //Appropriate data is available
                //Initialise needed variables and take the user to the main screen; start connections.
                BufferedReader reader = new BufferedReader(new FileReader(userInfo));
                LocalUser.setfName(reader.readLine());
                LocalUser.setlName(reader.readLine());
                LocalUser.setDirectory(new File(reader.readLine()));
                LocalUser.setMacAddress();
                LocalUser.setReceivePorts();
                LocalUser.setIpAddress();
                LocalUser.setKeys();
                ConnectionsManager.setPossIP();
                ConnectionsManager.setConnectionFailed(false);
                ConnectionsManager.setConnected(true);
                ConnectedController.setScreenIsOpen(true);
                try {
                    ChangeScene.change(screen, "connected.fxml");
                    ConnectionsManager.startConnections();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Runs when pressing the 'Settings' button. Takes user
     * to the settings screen.
     * @throws IOException
     */
    @FXML
    protected void onSettingsButtonClick() throws IOException {
        ChangeScene.change(screen,"settings.fxml");
    }

    /**
     * Runs when pressing the 'Information' button. Takes user
     * to the information screen.
     */
    @FXML
    protected void onInfoButtonClick() throws IOException {
        ChangeScene.change(screen, "info.fxml");
    }

    /**
     * Runs when pressing the 'Quit' button. Quits the program.
     */
    @FXML
    protected void onQuitClick(){
        System.exit(0);
    }

    /**
     * Initialises the start screen's key variables. It also determines,
     * based on the saved user data, whether some of the user's key data
     * is missing, which would in 'onStartClick()' trigger the opening of
     * a data input dialog.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File thisClass = new File(Start.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File mainFolderTemp = thisClass.getParentFile().getParentFile();
        String path = mainFolderTemp.getPath();
        while (path.indexOf("%20") != -1){
            int index = path.indexOf("%20");
            path = path.substring(0,index) + " " + path.substring(index+3);
        }
        //Reads the user information file to determine whether key data is missing.
        documents = new File(path + "/docs");
        File userInfo = new File(path + "/docs/userInfo.txt");
        BufferedReader reader = null;
        firstTime = false;
        try {
            reader = new BufferedReader(new FileReader(userInfo));
            for (int i = 0; i < 3; i++) {
                String line = reader.readLine();
                if (line == null) {
                    firstTime = true;
                    return;
                }
            }
        } catch (IOException ignored) {}
        if (!firstTime){
            try {
                reader = new BufferedReader(new FileReader(userInfo));
            } catch (FileNotFoundException e) { //Because the file could have been deleted
                firstTime = true;
                return;
            }
            try{
                //Set key variables in LocalUser class
                LocalUser.setfName(reader.readLine());
                LocalUser.setlName(reader.readLine());
                LocalUser.setDirectory(new File(reader.readLine()));
            } catch (IOException ignored) {}
        }
    }
}