package com.example.networkairlines;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Outbox implements Initializable {
    private static Outbox outboxController;
    private static Stage stage;
    private static boolean screenIsOpen = false;
    private static final ArrayList<Package> outgoing = new ArrayList<>();

    /**
     * Returns whether screen is open
     * @return boolean value
     */
    public static boolean getScreenIsOpen() {
        return screenIsOpen;
    }

    /**
     * Saves whether the screen has been opened/closed
     * @param b boolean value
     */
    public static void setScreenIsOpen(boolean b) {
        screenIsOpen = b;
    }

    /**
     * Returns controller of Outbox screen
     * @return Outbox object
     */
    public static Outbox getController() {
        return outboxController;
    }

    /**
     * Saves Outbox screen's controller for future use
     * @param controller
     */
    public static void setController(Outbox controller) {
        outboxController = controller;
    }

    /**
     * Returns FX stage object of Outbox screen
     * @return Stage object
     */
    public static Stage getStage() {
        return stage;
    }

    /**
     * Sets the Outbox screen's FX stage object for future use
     * @param stage
     */
    public static void setStage(Stage stage) {
        Outbox.stage = stage;
    }
    @FXML
    private Pane pane;

    /**
     * Returns all the outgoing file packages associated with send requests on behalf of this user (yet to be accepted)
     * @return ArrayList object of Package objects
     */
    public static ArrayList<Package> getOutgoing() {
        return outgoing;
    }

    /**
     * Finds, within the outgoing packages, the Package object associated to a certain SendRequest/Response/Cancel object through its ID and receiver.
     * @param transmission could be SendRequest, Response, or Cancel object (Transmission inheritors)
     * @return Package object (Transmission inheritor)
     */
    public static Package find(Transmission transmission) {
        //Performs linear search by looping through the outgoing packages and checking for equality of ID and receiver in each package compared to the transmission parameter.
        for (int i = 0; i < outgoing.size(); i++) {
            Package p = outgoing.get(i);
            if (p.getReceiver() != null) {
                if (p.getID() == transmission.getID() && p.getReceiver().getMacAddress().equals(transmission.getReceiver().getMacAddress())) {
                    return p;
                }
            } else {
                if (p.getID() == transmission.getID()) {
                    for (int j = 0; j < p.getReceivers().size(); j++) {
                        if (transmission.getReceiver().getMacAddress().equals(p.getReceivers().get(j).getMacAddress())) {
                            return p;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Adds a Package to the outgoing packages associated with some sent request
     * @param p Package object to add
     */
    public static void add(Package p) {
        outgoing.add(p);
    }

    /**
     * Removes a Package (associated with a SendRequest/Response/Cancel object) from the outgoing ones when it has been sent/cancelled.
     * @param transmission Associated Transmission object which dictates the Package to remove
     */
    public static void remove(Transmission transmission) {
        //Performs linear search by looping through the ArrayList of packages
        if (transmission.getReceiver() != null) {
            for (int i = 0; i < outgoing.size(); i++) {
                Package p = outgoing.get(i);
                if (p.getReceiver() != null) {
                    if (p.getID() == transmission.getID() && p.getReceiver().getMacAddress().equals(transmission.getReceiver().getMacAddress())) {
                        outgoing.remove(i);
                        break;
                    }
                } else {
                    if (p.getID() == transmission.getID()) {
                        boolean control = false;
                        for (int j = 0; j < p.getReceivers().size(); j++) {
                            if (transmission.getReceiver().getMacAddress().equals(p.getReceivers().get(j).getMacAddress())) {
                                p.addReceived();
                                control = true;
                                break;
                            }
                        }
                        if (control) {
                            break;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < outgoing.size(); i++) {
                Package p = outgoing.get(i);
                if (transmission == p) {
                    outgoing.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * Runs when pressing the 'Close' button on the Outbox screen. It closes the screen.
     */
    @FXML
    public void close() {
        Stage stage = (Stage) (pane.getScene().getWindow());
        screenIsOpen = false;
        stage.hide();
    }

    /**
     * Updates the outbox screen by displaying all the outgoing packages associated with send requests.
     */
    public void updateOutbox() {
        HBox[] boxes = new HBox[outgoing.size()];
        for (int i = 0; i < outgoing.size(); i++) {
            Package p = outgoing.get(i);
            HBox bigBox = new HBox();
            bigBox.setStyle("-fx-border-color: #2F6690; -fx-border-radius: 10; -fx-border-width: 2");
            bigBox.setPadding(new Insets(0, 7, 0, 7));
            bigBox.setPrefWidth(331);
            bigBox.setPrefHeight(37);
            bigBox.setLayoutX(20);
            bigBox.setLayoutY(10 + 37*i + 4);

            VBox labels = new VBox();
            labels.setPrefWidth(211);
            labels.setPrefHeight(35);
            labels.setLayoutX(0);
            labels.setLayoutY(0);

            Label user = new Label();
            if (p.getReceiver() == null) {
                user.setText("Multiple users selected");
                user.setStyle("-fx-text-fill: #123A57");
            } else {
                user.setText(p.getReceiver().getlName() + ", " + p.getReceiver().getfName());
                user.setStyle("-fx-text-fill: #123A57");
            }

            Label size = new Label();
            String totalSize = String.valueOf(((double) p.getSize()/1073741824));
            size.setText("Total size: " + totalSize.substring(0,4) + " GB");
            size.setStyle("-fx-text-fill: #123A57");

            HBox buttons = new HBox();
            buttons.setPrefWidth(200);
            buttons.setPrefHeight(100);
            buttons.setLayoutX(0);
            buttons.setLayoutY(0);
            buttons.setSpacing(3);
            buttons.setAlignment(Pos.CENTER_RIGHT);

            Button view = new Button();
            view.setText("View more");
            view.setPrefWidth(75);
            view.setPrefHeight(26);
            view.setStyle("-fx-text-fill: #F4B266; -fx-background-radius: 10; -fx-background-color: #2F6690");
            view.setOnAction(event -> {});

            Button cancel = new Button();
            cancel.setText("Cancel");
            cancel.setPrefWidth(55);
            cancel.setPrefHeight(26);
            cancel.setStyle("-fx-text-fill: #F4B266; -fx-background-radius: 10; -fx-background-color: #2F6690");
            cancel.setOnAction(event -> {
                Outbox.remove(p);
                updateOutbox();
                ConnectedController.getController().update();
                if (p.getReceiver() != null) {
                    Cancel cancelTransmission = new Cancel(p.getSender(), p.getReceiver(), 3, p.getID());
                    try {
                        byte[][] encryptedData = cancelTransmission.encrypt(p.getReceiver().getPublicKey());
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(p.getReceiver().getIpAddress(), p.getReceiver().getReceivePort()));
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(encryptedData);
                        oos.close();
                        socket.close();
                    } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException |
                             InvalidKeyException | NoSuchPaddingException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Outbox.remove(p);
                    updateOutbox();
                    ConnectedController.getController().update();
                    for (int j = 0; j < p.getReceivers().size(); j++) {
                        User u = p.getReceivers().get(j);
                        Cancel cancelTransmission = new Cancel(p.getSender(), u, 3, p.getID());
                        try {
                            byte[][] encryptedData = cancelTransmission.encrypt(u.getPublicKey());
                            Socket socket = new Socket();
                            socket.connect(new InetSocketAddress(u.getIpAddress(), u.getReceivePort()));
                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            oos.writeObject(encryptedData);
                            oos.close();
                            socket.close();
                        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException |
                                 InvalidKeyException | NoSuchPaddingException | IOException ignored) {}
                    }
                }
            });

            Platform.runLater(() -> {
                labels.getChildren().add(user);
                labels.getChildren().add(size);
                buttons.getChildren().add(cancel);
                buttons.getChildren().add(view);
                bigBox.getChildren().add(labels);
                bigBox.getChildren().add(buttons);
            });
            boxes[i] = bigBox;
        }
        Platform.runLater(() -> {
            pane.getChildren().clear();
        });
        for (HBox box : boxes) {
            Platform.runLater(() -> {
                pane.getChildren().add(box);
            });
        }
    }

    /**
     * Initialises the outbox screen by updating it and displaying all the outgoing packages
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateOutbox();
    }
}