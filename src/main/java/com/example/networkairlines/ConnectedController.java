package com.example.networkairlines;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ConnectedController implements Initializable {
    private static boolean screenIsOpen = false;
    private static ConnectedController controller;

    /**
     * Returns controller of the main screen
     * @return ConnectedController object
     */
    public static ConnectedController getController() {
        return controller;
    }

    /**
     * Saves controller of the main screen
     * @param controller
     */
    public static void setController(ConnectedController controller) {
        ConnectedController.controller = controller;
    }

    /**
     * Returns if the main screen is open
     * @return boolean value
     */
    public static boolean getScreenIsOpen() {
        return screenIsOpen;
    }

    /**
     * Saves if the main screen is open
     * @param b a boolean value
     */
    public static void setScreenIsOpen(boolean b) {
        screenIsOpen = b;
    }
    @FXML
    private AnchorPane screen;
    @FXML
    private TextField searchBox;

    /**
     * Returns screen object of the main screen
     * @return AnchorPane object
     */
    public AnchorPane getScreen() {
        return screen;
    }

    /**
     * Returns the text field (search box) object of the main screen
     * @return TextField object
     */
    private TextField getTextField() {
        return searchBox;
    }

    /**
     * Returns the text inputted into the search box
     * @return String
     */
    public static String getSearchText() {
        return controller.getTextField().getText();
    }
    private static boolean multipleUsers = false;
    ArrayList<User> usersToSend = new ArrayList<>();
    @FXML
    private Button toSendDialog;

    /**
     * Takes user from main to start screen when user chooses to disconnect
     * @throws IOException if fxml file does not exist (though it does)
     */
    @FXML
    protected void onDisconnectClick() throws IOException {
        //Changes appropiate variables
        ConnectionsManager.setConnected(false);
        screenIsOpen = false;
        //Closes other associated screens (Inbox/Outbox)
        if (Outbox.getScreenIsOpen()) {
            Outbox.getController().close();
        }
        if (Inbox.getScreenIsOpen()) {
            Inbox.getController().close();
        }
        //Finally changes the screen
        ChangeScene.change(screen, "start.fxml");
    }

    /**
     * Opens Outbox screen to let user see their outgoing send requests
     * @throws IOException if outbox.fxml does not exist
     */
    @FXML
    protected void onViewOutboxClick() throws IOException {
        //If screen is already open, bring it to front
        if (Outbox.getScreenIsOpen()) {
            Outbox.getStage().toFront();
            return;
        }
        //Change and save appropriate variables including the controller object
        //Load screen from fxml file
        Outbox.setScreenIsOpen(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("outbox.fxml"));
        Parent root = loader.load();
        Outbox.setController(loader.getController());
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("Outbox");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            Outbox.setScreenIsOpen(false);
        });
        stage.show();
        Outbox.setStage(stage);
    }

    /**
     * Opens Inbox screen to let user see received send requests
     * @throws IOException if inbox.fxml does not exist
     */
    @FXML
    protected void onViewInboxClick() throws IOException {
        //If screen is already open, bring to front
        if (Inbox.getScreenIsOpen()) {
            Inbox.getStage().toFront();
            return;
        }
        //Save appropriate variables and load screen from fxml file
        Inbox.setScreenIsOpen(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("inbox.fxml"));
        Parent root = loader.load();
        Inbox.setController(loader.getController());
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setOnCloseRequest(event1 -> {
            Inbox.setScreenIsOpen(false);
        });
        stage.setTitle("Inbox");
        stage.setScene(scene);
        stage.show();
        Inbox.setStage(stage);
    }
    @FXML
    Pane basePane;
    @FXML
    Button userPlurality;

    /**
     * Runs if the button 'Send to multiple users'/'Send to a single user' is pressed.
     * On the multiple user mode, the user selects all the users desired before going to the send dialog.
     * On single user mode, the user selects only one user and goes directly to the send dialog.
     */
    @FXML
    protected void multipleUserMode() {
        if (!multipleUsers) {
            multipleUsers = true;
            userPlurality.setText("Send to a single user");
        } else {
            multipleUsers = false;
            userPlurality.setText("Send to multiple users");
        }

    }

    /**
     * Runs when user has selected multiple users and presses the button to go to the send dialog.
     * @throws IOException if sendDialog.fxml does not exist
     */
    @FXML
    protected void multipleUserSendDialog() throws IOException {
        SendDialog.setUsers(usersToSend);
        screenIsOpen = false;
        ChangeScene.change(screen, "sendDialog.fxml");
    }

    /**
     * Runs when on single user mode and user clicks a user. Takes the user to the send dialog to send a send request
     * to this user.
     * @param user User object which will be the receiver of a send request
     * @throws IOException
     */
    private void toSendDialog(User user) throws IOException {
        SendDialog.setUser(user);
        screenIsOpen = false;
        ChangeScene.change(screen, "sendDialog.fxml");
    }
    /**
     * Updates the screen showing connected users based
     * on the background network scanning.
     * Each user shown is a button. When clicked on, the
     * send dialog is opened to send to this clicked user.
     */
    @FXML
    public void updateViewList() {
        //Button dimensions
        double buttonHeight = 40;
        double buttonWidth = 140;
        int height = 0;
        //Get all the User objects connected to the network
        ArrayList<User> connectedUsers = ConnectionsManager.sort(ConnectionsManager.getConnectedUsers());
        Button[] buttons = new Button[connectedUsers.size()];
        //Goes through each user and adds it as a button
        for (int i = 0; i < connectedUsers.size(); i++){
            /*
            The rows of users shown are of three
            This checks if a new row is needed
            (increase in height).
             */
            int indexInRow = i % 3;
            if (indexInRow == 0 && i != 0) {
                height++;
            }
            User user = connectedUsers.get(i);
            //Avoid IllegalStateException
            int finalHeight = height;
            //Sets button dimenstions and position
            Button button = new Button();
            button.setText(user.getfName() + " " + user.getlName());
            button.setPrefWidth(buttonWidth);
            button.setPrefHeight(buttonHeight);
            /*
            Pattern: determines position as a function
            of the number of rows and the index in the row.
            Considers spacing and button dimensions.
             */
            button.setLayoutY(50*finalHeight + 10);
            button.setLayoutX(175*indexInRow + 10);
            /*
            If user has been selected as a receiver amongst
            multiple, the button's colour should be different.
             */
            final boolean[] selectedForMultiple = {false};
            if (multipleUsers) {
                for (int j = 0; j < usersToSend.size(); j++) {
                    /*
                    Checks each user's (in the multiple user
                    sending list) Mac address to see if this
                    user is on there.
                     */
                    String s = usersToSend.get(j).getMacAddress();
                    if (s.equals(user.getMacAddress())) {
                        selectedForMultiple[0] = true;
                        break;
                    }
                }
            }
            if (!(multipleUsers && selectedForMultiple[0])) {
                //If selected amongst multiple users
                button.setStyle("-fx-background-color: #2F6690; -fx-background-radius : 10; -fx-text-fill: #F4B266");
            } else{
                button.setStyle("-fx-background-color: #F4B266; -fx-background-radius : 10; -fx-text-fill: #2F6690");
            }
            button.setOnAction(event -> {
                if (!multipleUsers) {
                    /*
                    If on single user sending mode,
                    just take user to Send Dialog
                    when this button is clicked.
                     */
                    try {
                        toSendDialog(user);
                    } catch (IOException e) {
                        //Should never happen
                    }
                } else {
                    /*
                    Otherwise change its colour to
                    indicate the user is selected among
                    multiple.
                     */
                    if (!selectedForMultiple[0]) {
                        //Add user to multiple user send list
                        button.setStyle("-fx-background-color: #F4B266; -fx-background-radius : 10; -fx-text-fill: #2F6690");
                        usersToSend.add(user);
                        if (usersToSend.size() == 1) {
                            toSendDialog.setVisible(true);
                        }
                    } else {
                        //Remove user from multiple user send list
                        button.setStyle("-fx-background-color: #2F6690; -fx-background-radius : 10; -fx-text-fill: #F4B266");
                        for (int j = 0; j < usersToSend.size(); j++) {
                            if (user.getMacAddress().equals(usersToSend.get(j).getMacAddress())) {
                                usersToSend.remove(j);
                                break;
                            }
                        }
                        if (usersToSend.size() == 0) {
                            toSendDialog.setVisible(false);
                        }
                    }
                }
            });
            buttons[i] = button;
        }
        //Add the buttons (having set all their properties)
        Platform.runLater(() -> basePane.getChildren().clear());
        for (Button button : buttons) {
            Platform.runLater(() -> basePane.getChildren().add(button));
        }
    }
    @FXML
    Label inboxIndicator;

    /**
     * Returns number of send requests in the inbox
     * @return integer
     */
    public int getInboxNum() {
        return Inbox.getIncoming().size();
    }
    @FXML
    Label outboxIndicator;

    /**
     * Returns number of outgoing send requests (in the outbox)
     * @return integer
     */
    public int getOutboxNum() {
        return Outbox.getOutgoing().size();
    }

    /**
     * Updates the messages on the main screen of how many incoming/outgoing send requests the user has.
     * Runs when a send request is removed/is added to either the Inbox or the Outbox.
     */
    public void update() {
        Platform.runLater(() -> {
            inboxIndicator.setText("You have " + getInboxNum() + " incoming send requests.");
            outboxIndicator.setText("You have " + getOutboxNum() + " outgoing send requests.");
        });
    }

    /**
     * Initialises appropriate variables and screen characteristics when opening the screen
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        toSendDialog.setVisible(false);
        update();
        multipleUsers = false;
        usersToSend = new ArrayList<>();
        updateViewList();
    }
}