package com.example.networkairlines;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Inbox implements Initializable {
    private static final ArrayList<SendRequest> incoming = new ArrayList<>();
    private static Inbox controller;
    private static boolean screenIsOpen;
    private static Stage stage;

    /**
     * Returns controller of inbox screen
     * @return InboxController object
     */
    public static Inbox getController() {
        return controller;
    }

    /**
     * Saves inbox's controller
     * @param controller
     */
    public static void setController(Inbox controller) {
        Inbox.controller = controller;
    }

    /**
     * Returns whether screen is open
     * @return boolean value
     */
    public static boolean getScreenIsOpen() {
        return screenIsOpen;
    }

    /**
     * Saves whether screen has been opened/closed
     * @param b boolean
     */
    public static void setScreenIsOpen(boolean b) {
        Inbox.screenIsOpen = b;
    }

    /**
     * Returns FX stage of the Inbox screen
     * @return Stage object
     */
    public static Stage getStage() {
        return stage;
    }

    /**
     * Saves stage object for the inbox screen for future use
     * @param stage stage object
     */
    public static void setStage(Stage stage) {
        Inbox.stage = stage;
    }
    @FXML
    private Pane pane;

    /**
     * Runs when user presses the close button in the inbox.
     */
    @FXML
    public void close() {
        //Merely closes the FX scene
        Stage stage = (Stage) (pane.getScene().getWindow());
        screenIsOpen = false;
        stage.hide();
    }

    /**
     * Returns all the incoming send requests
     * @return ArrayList object of SendRequest objects (Transmission inheritors)
     */
    public static ArrayList<SendRequest> getIncoming() {
        return incoming;
    }

    /**
     * Adds a SendRequest object to the ArrayList of all the incoming SendRequests
     * @param sendRequest
     */
    public static void add(SendRequest sendRequest) {
        incoming.add(sendRequest);
        if (screenIsOpen) {
            controller.updateInbox();
        }
    }

    /**
     * Removes a SendRequest from the ArrayList of all the incoming SendRequests if it has been accepted/rejected
     * @param transmission uses any Transmission inheritor associated to the same file package transmission to identify the SendRequest to
     * be removed through its ID instance variable. For example, a Cancel object could be used to find the associated SendRequest and remove it.
     */
    public static void remove(Transmission transmission) {
        //Only removes if both the transmission ID and receiver are equal
        String receiverMac = transmission.getReceiver().getMacAddress();
        if (!receiverMac.equals(LocalUser.getMacAddress())) {
            return;
        }
        int removeID = transmission.getID();
        for (int i = 0; i < incoming.size(); i++) {
            Transmission t = incoming.get(i);
            if (t.getID() == removeID) {
                incoming.remove(i);
                break;
            }
        }
    }

    /**
     * Updates the inbox screen by displaying all the incoming send requests.
     */
    public void updateInbox() {
        //Each SendRequest is represented by an HBox filled with interactive elements which allow the user to discover and interact with the SendRequest.
        HBox[] boxes = new HBox[incoming.size()];
        for (int i = 0; i < incoming.size(); i++) {
            SendRequest request = incoming.get(i);

            //Sets the HBox's dimensions
            HBox bigBox = new HBox();
            bigBox.setStyle("-fx-border-color: #2F6690; -fx-border-radius: 10; -fx-border-width: 2");
            bigBox.setPadding(new Insets(0, 7, 0, 7));
            bigBox.setPrefWidth(331);
            bigBox.setPrefHeight(37);
            bigBox.setLayoutX(20);
            bigBox.setLayoutY(10 + 37*i + 4);

            //Inside, there is a VBox filled with labels displaying info on the SendRequest
            VBox labels = new VBox();
            labels.setPrefWidth(211);
            labels.setPrefHeight(35);
            labels.setLayoutX(0);
            labels.setLayoutY(0);

            //Display sender name
            Label user = new Label();
            user.setText(request.getSender().getlName() + ", " + request.getSender().getfName());
            user.setStyle("-fx-text-fill: #123A57");

            //Display package size
            Label size = new Label();
            String totalSize = String.valueOf((double) request.getSize()/1073741824);
            size.setText("Total size: " + totalSize.substring(0,4) + " GB");
            size.setStyle("-fx-text-fill: #123A57");

            //There will also be an HBox filled with buttons so the user can interact with the SendRequest and send a response
            HBox buttons = new HBox();
            buttons.setPrefWidth(300);
            buttons.setPrefHeight(100);
            buttons.setLayoutX(0);
            buttons.setLayoutY(0);
            buttons.setSpacing(3);
            buttons.setAlignment(Pos.CENTER_RIGHT);

            //Each SendRequest has a reject button next to it which will send a 'false' Response object to the sender. The file package will not be sent.
            Button reject = new Button();
            reject.setText("Reject");
            reject.setPrefWidth(75);
            reject.setPrefHeight(26);
            reject.setStyle("-fx-text-fill: #F4B266; -fx-background-radius: 10; -fx-background-color: #2F6690");
            reject.setOnAction(event -> {
                Inbox.remove(request);
                updateInbox();
                ConnectedController.getController().update();
                Response response = new Response(request.getSender(), request.getReceiver(), 2, request.getID(), false);
                Socket toRespond = new Socket();
                try {
                    toRespond.connect(new InetSocketAddress(request.getSender().getIpAddress(), request.getSender().getReceivePort()));
                    ObjectOutputStream oos = new ObjectOutputStream(toRespond.getOutputStream());
                    oos.writeObject(response);
                    oos.close();
                    toRespond.close();
                } catch (IOException e) {
                    //Handle exception
                }
            });

            //Every SendRequest will also have an accept button which sends a 'true' Response object to the sender and wait for the file package.
            Button accept = new Button();
            accept.setText("Accept");
            accept.setPrefWidth(75);
            accept.setPrefHeight(26);
            accept.setStyle("-fx-text-fill: #F4B266; -fx-background-radius: 10; -fx-background-color: #2F6690");
            accept.setOnAction(event -> {
                Inbox.remove(request);
                updateInbox();
                ConnectedController.getController().update();
                Response response = new Response(request.getSender(), request.getReceiver(), 2, request.getID(), true);
                try {
                    byte[][] encryptedData = response.encrypt(response.getSender().getPublicKey());
                    Socket toRespond = new Socket();
                    toRespond.connect(new InetSocketAddress(request.getSender().getIpAddress(), request.getSender().getReceivePort()));
                    ObjectOutputStream oos = new ObjectOutputStream(toRespond.getOutputStream());
                    oos.writeObject(encryptedData);
                    oos.close();
                    toRespond.close();
                } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException |
                         InvalidKeyException | NoSuchPaddingException | IOException e) {
                    e.printStackTrace();
                }
                ServerSocket server;
                Socket toReceive;
                try {
                    server = new ServerSocket(LocalUser.getBackup());
                    toReceive = server.accept();
                    ObjectInputStream ois = new ObjectInputStream(toReceive.getInputStream());
                    int totalChunks = ois.readInt();
                    byte[][] packageData = new byte[totalChunks][];
                    for (int j = 0; j < totalChunks; j++) {
                        byte[] chunk = (byte[]) ois.readObject();
                        packageData[j] = chunk;
                    }
                    Transmission.decryptAndSave(packageData);
                } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                         BadPaddingException | InvalidKeyException ignored) {}
                catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });

            //View more allows user to see the sender's description of the file package.
            Button viewMore = new Button();
            viewMore.setText("Description");
            viewMore.setPrefWidth(100);
            viewMore.setPrefHeight(26);
            viewMore.setStyle("-fx-text-fill: #F4B266; -fx-background-radius: 10; -fx-background-color: #2F6690");
            viewMore.setOnAction(event -> {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("viewmore.fxml"));
                Parent root = null;
                try {
                    root = loader.load();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                NotificationController controller = loader.getController();
                controller.setSource(this.getClass());
                controller.setText(request.getMess());
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setResizable(false);
                stage.setTitle("Package description");
                stage.setScene(scene);
                stage.show();
            });

            Platform.runLater(() -> {
                labels.getChildren().add(user);
                labels.getChildren().add(size);
                buttons.getChildren().add(viewMore);
                buttons.getChildren().add(accept);
                buttons.getChildren().add(reject);
                bigBox.getChildren().add(labels);
                bigBox.getChildren().add(buttons);
            });
            boxes[i] = bigBox;
        }
        Platform.runLater(() -> {
            pane.getChildren().clear();
        });

        //Displays all the SendRequests
        for (HBox box : boxes) {
            Platform.runLater(() -> {
                pane.getChildren().add(box);
            });
        }
    }

    /**
     * Function initialising the inbox screen by 'updating' it and displaying all the incoming send requests.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateInbox();
    }
}