package com.example.networkairlines;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private File destinationDir;
    @FXML
    private Pane screen;
    @FXML
    private TextField fName;
    @FXML
    private TextField lName;
    @FXML
    private TextField orgName;
    @FXML
    private TextField depName;
    @FXML
    private Label errorMess;
    @FXML
    private Label directory;

    /**
     * Runs when pressing 'Save and back'. Saves the settings to a text file
     * and in the static variables of the LocalUser class.
     * @throws IOException
     */
    @FXML
    protected void onContinue() throws IOException {
        if (fName.getText().equals("") || lName.getText().equals("") || destinationDir == null){
            //Display error message if insufficient data is inputted.
            errorMess.setVisible(true);
        } else {
            File thisClass = new File(Start.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            File mainFolderTemp = thisClass.getParentFile().getParentFile();
            String path = mainFolderTemp.getPath();
            while (path.contains("%20")){
                int index = path.indexOf("%20");
                path = path.substring(0,index) + " " + path.substring(index+3);
            }
            //Save data to LocalUser variables and a text file.
            StartController.documents = new File(path + "/docs");
            File userInfo = new File(StartController.documents.getAbsolutePath() + "/userInfo.txt");
            System.out.println(userInfo.getAbsolutePath());
            FileWriter writeUserInfo = new FileWriter(userInfo);
            writeUserInfo.write(fName.getText() + "\n");
            LocalUser.setfName(fName.getText());
            writeUserInfo.write(lName.getText() + "\n");
            LocalUser.setlName(lName.getText());
            writeUserInfo.write(destinationDir.getAbsolutePath() + "\n");
            LocalUser.setDirectory(new File(destinationDir.getAbsolutePath()));
            writeUserInfo.close();

            //Returns to start screen
            ChangeScene.change(screen,"start.fxml");
        }
    }
    /**
     * Run when pressing the 'Attach folder' button. Allows the user to select a
     * folder from their files to add to the files to send.
     */
    @FXML
    protected void selectDirectory(){
        DirectoryChooser dChooser = new DirectoryChooser();
        File directory = dChooser.showDialog(null);
        if (directory != null){
            destinationDir = directory;
            this.directory.setText(directory.getName());
        }
    }

    /**
     * Runs when pressing 'Cancel'. Returns to start screen without saving
     * any changes.
     * @throws IOException
     */
    @FXML
    protected void onCancelClick() throws IOException {
        ChangeScene.change(screen, "start.fxml");
    }

    /**
     * Initialises screen and basic variables when screen is opened.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fName.setText(LocalUser.getfName());
        lName.setText(LocalUser.getlName());
        destinationDir = LocalUser.getDirectory();
        directory.setText(destinationDir.getName());
    }
}