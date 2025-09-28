package com.example.networkairlines;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class SendDialog implements Initializable {
    private static User user;
    private static ArrayList<User> users;
    private static boolean multipleUserMode = false;
    private static ArrayList<File> selectedFiles = new ArrayList<>();
    private static ArrayList<TrackedFile> filesToSend = new ArrayList<>();
    private static SendDialog controller;
    private static volatile CountDownLatch duplicateLatch = null;

    /**
     * Returns controller of Send Dialog
     * @return SendDialog object
     */
    public static SendDialog getController() {
        return controller;
    }

    /**
     * Saves the controller object for the Send Dialog
     * @param controller
     */
    public static void setController(SendDialog controller) {
        SendDialog.controller = controller;
    }

    /**
     * In the case of the package being sent to a single user, this function sets the user that will be sent the file (User object)
     * @param user
     */
    public static void setUser(User user) {
        SendDialog.user = user;
    }

    /**
     * In the case of multiple receivers, this sets all the users that will be sent the file (ArrayList of User objects)
     * @param users
     */
    public static void setUsers(ArrayList<User> users) {
        SendDialog.users = users;
    }

    /**
     * Runs when 'Back' button is clicked. No send request is sent and this takes the user back to main screen from send dialog.
     * @throws IOException if connected.fxml does not exist.
     */
    @FXML
    protected void onBackClick() throws IOException {
        user = null;
        users = null;
        selectedFiles = new ArrayList<>();
        ConnectedController.setScreenIsOpen(true);
        ChangeScene.change(screen, "connected.fxml");
    }

    @FXML
    private AnchorPane screen;

    /**
     * Returns FX pane object (screen) of Send Dialog
     * @return AnchorPane object
     */
    public AnchorPane getScreen() {
        return screen;
    }

    /**
     * Runs when pressing the 'Attach a file' button. Opens a file chooser which adds the chosen files to the list of selected files.
     */
    @FXML
    protected void selectFiles() {
        FileChooser chooser = new FileChooser();
        List<File> files;
        try{
            files = chooser.showOpenMultipleDialog(null);
            assert files != null;
            for (File file : files) {
                selectedFiles.add(file);
                updateSelectedFilesView();
            }
        } catch (NullPointerException ignored) {}
    }

    /**
     * Runs when pressing the 'Clear files' button. Removes all the selected files.
     */
    @FXML
    protected void clearFiles() {
        for (int i = 0; i < selectedFiles.size(); i++) {
            File file = selectedFiles.get(i);
            if (file.isFile()){
                selectedFiles.remove(i);
                i--;
            }
        }
        updateSelectedFilesView();
    }

    /**
     * Run when pressing the 'Attach folder' button. Allows the user to select a
     * folder from their files to add to the files to send.
     */
    @FXML
    protected void selectFolders() {
        DirectoryChooser chooser = new DirectoryChooser();
        try {
            File directory = chooser.showDialog(null);
            selectedFiles.add(directory);
            updateSelectedFilesView();
        } catch (NullPointerException ignored) {}
    }

    /**
     * Runs when pressing the 'Clear' button below the folders box. Clears all
     * selected folders from the list of files to send.
     */
    @FXML
    protected void clearFolders() {
        for (int i = 0; i < selectedFiles.size(); i++){
            File file = selectedFiles.get(i);
            if (file.isDirectory()){
                selectedFiles.remove(i);
                i--;
            }
        }
        updateSelectedFilesView();
    }
    @FXML
    private TextArea message;

    /**
     * Runs when user presses 'Send' after having selected the user(s) (in the
     * main screen) and all the files to send (in the send dialog). Creates and
     * sends a SendRequest Transmission object for the receiver to accept/reject.
     * @throws IOException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    @FXML
    protected void onSendClick() throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String mess = message.getText();
        //Creates a list of TrackedFile objects for with all selected files/folders.
        for (File file : selectedFiles) {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            if (basicFileAttributes.isRegularFile()) {
                TrackedFile tFile = new TrackedFile("", file);
                filesToSend.add(tFile);
            } else if (basicFileAttributes.isDirectory()) {
                /*
                If the file is a directory, recursively go through all directories
                inside it and add all the files inside them as TrackedFile objects.
                 */
                expandDirectory(file, file.getName());
            }
        }
        /*
        Generates a unique ID for all the transmissions relating to
        the sending of this specific file package.
         */
        int id = (int)(Math.random()*Integer.MAX_VALUE);
        //User object of sender
        User localUser = new User(LocalUser.getIpAddress(), LocalUser.getMacAddress(), LocalUser.getfName(), LocalUser.getlName(), LocalUser.getDepartment(), LocalUser.getOrgName(), LocalUser.getReceivePort(), LocalUser.getBackup(), LocalUser.getPublicKey());
        if (!multipleUserMode) {
            //User has not specified to send to multiple users and has selected one.
            Package filePackage = new Package(localUser, user, 3, id, filesToSend);
            SendRequest sendRequest = new SendRequest(localUser, user, 1, id, mess, filePackage.getSize());
            byte[][] encrypted = sendRequest.encrypt(user.getPublicKey());
            Socket socket = new Socket();
            try {
                //Send the SendRequest to receiver
                socket.connect(new InetSocketAddress(user.getIpAddress(), user.getReceivePort()));
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(encrypted);
                oos.close();
                //Add Package to Outbox
                Outbox.add(filePackage);
                if (Outbox.getScreenIsOpen()) {
                    Outbox.getController().updateOutbox();
                }
                System.out.println("SendRequest sent");
            } catch (SocketException e) {
                //User is no longer found on the network, cancel transmission.
                Outbox.remove(filePackage);
                ConnectedController.getController().update();
                //Display error notification.
                FXMLLoader loader = new FXMLLoader(StartController.class.getResource("notification.fxml"));
                Scene scene = new Scene(loader.load());
                NotificationController controller = loader.getController();
                controller.setSource(SendDialog.class);
                controller.setText("Transmission error: failed to connect with recipient.");
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setOnCloseRequest(event -> {
                    try {
                        ChangeScene.change(screen, "connected.fxml");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                stage.show();
                screen.setDisable(true);
                ConnectedController.setScreenIsOpen(true);
                return;
            }
            //Return to main screen
            user = null;
            ConnectedController.setScreenIsOpen(true);
            ChangeScene.change(screen, "connected.fxml");
        } else {
            //User has specified to send to multiple users and has selected them.
            Package filePackage = new Package(localUser, users, 3, id, filesToSend);
            //Add package to outbox.
            Outbox.add(filePackage);
            if (Outbox.getScreenIsOpen()) {
                Outbox.getController().updateOutbox();
            }
            //Send a SendRequest to each user selected.
            for (User user : users) {
                SendRequest sendRequest = new SendRequest(localUser, user, 1, id, mess, filePackage.getSize());
                byte[][] encrypted = sendRequest.encrypt(user.getPublicKey());
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(user.getIpAddress(), user.getReceivePort()));
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(encrypted);
                    oos.close();
                    System.out.println("SendRequest sent");
                } catch (SocketException e) {
                    continue;
                }
            }
            //Return to main screen
            users = null;
            ConnectedController.setScreenIsOpen(true);
            ChangeScene.change(screen, "connected.fxml");
        }
    }
    @FXML
    private Pane fileBox;
    @FXML
    private Pane directoryBox;
    @FXML
    private Label fileLabel;
    @FXML
    private Label directoryLabel;
    @FXML
    private Button clearFiles;
    @FXML
    private Button clearDirectories;
    /**
     * Updates graphics when user has attached a file to the sending
     * package (through a file-choosing dialog).
     */
    private void updateSelectedFilesView() {
        try{
            if (duplicateLatch != null){
                duplicateLatch.await();
                duplicateLatch = null;
            }
        } catch (InterruptedException ignored) {}
        Platform.runLater(() -> fileBox.getChildren().removeIf(node -> !node.equals(fileLabel) && !node.equals(clearFiles)));
        Platform.runLater(() -> directoryBox.getChildren().removeIf(node -> !node.equals(directoryLabel) && !node.equals(clearDirectories)));
        int files = 0;
        int directories = 0;
        /*
        Add an interactive label on a list in the Send Dialog for each
        file attached to the send package so the user can keep track
        and modify the list.
         */
        for (File file : selectedFiles) {
            //Set basic attributes of label.
            Label label = new Label(file.getName()); //Sets text
            label.setStyle("-fx-text-fill: #114162"); //Sets colour
            label.setPadding(new Insets(0, 5, 0, 5));
            /*
            When hovering over each label, the label's immediate
            background changes colour so the user knows that file
            is 'temporarily selected'.
             */
            label.setOnMouseEntered(event -> {
                label.setStyle("-fx-text-fill: #F4B266; -fx-background-radius: 10; -fx-background-color: #2F6690");
            });
            label.setOnMouseExited(event -> {
                label.setStyle("-fx-text-fill: #114162; -fx-background-color: null");
            });
            //Add folders to a separate list than the files.
            if (file.isDirectory()) {
                //Set position
                label.setLayoutX(7);
                /*
                Pattern: the y-position is determined by an
                initial position and how many directories
                have been processed.
                 */
                label.setLayoutY(17 + 17*directories);
                //Avoid IllegalStateException
                Platform.runLater(() -> directoryBox.getChildren().add(label));
                directories++;
                /*
                When clicking on a label on the list it removes
                the corresponding folder from the send list.
                 */
                label.setOnMouseClicked(event -> {
                    for (int i = 0; i < selectedFiles.size(); i++){
                        if (selectedFiles.get(i).equals(file)){
                            selectedFiles.remove(i);
                            break;
                        }
                    }
                    updateSelectedFilesView();
                });
            } else {
                label.setLayoutX(7);
                label.setLayoutY(17 + 17*files);
                //Avoid IllegalStateException
                Platform.runLater(() -> fileBox.getChildren().add(label));
                files++;
                //Idem, remove file from send list if clicked on
                label.setOnMouseClicked(event -> {
                    for (int i = 0; i < selectedFiles.size(); i++){
                        if (selectedFiles.get(i).equals(file)){
                            selectedFiles.remove(i);
                            break;
                        }
                    }
                    updateSelectedFilesView();
                });
            }
        }
    }

    /**
     * Recursive function run when creating TrackedFiles for all files
     * in a directory. For all the files in a certain directory, it creates
     * TrackedFile objects and adds them into the 'files' sending list with
     * their reference as to what folders they belong in. For each directory
     * within a directory, this function is called again and the reference
     * path that would be given to the files within the next directory would
     * be expanded.
     * @param directory is the starting folder
     * @param referencePath is the path of this folder relative to the absolute starting folder
     * @throws IOException
     */
    private void expandDirectory(File directory, String referencePath) throws IOException {
        //Lists all files and folders in directory
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            /*
            Determines path of this file based on referencePath
            built up through all the recursive calls.
             */
            String path = referencePath + File.separator + file.getName();
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            if (basicFileAttributes.isRegularFile()) {
                /*
                If it's a file, add its TrackedFile to the list
                of TrackedFiles to send.
                 */
                TrackedFile tFile = new TrackedFile(referencePath, file);
                filesToSend.add(tFile);
            } else if (basicFileAttributes.isDirectory()) {
                expandDirectory(file, path);
            }
        }
    }
    @FXML
    private Label selectedUser;

    /**
     * Initialises screen and basic variables when the Send Dialog is opened.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        multipleUserMode = false;
        selectedFiles = new ArrayList<>();
        //Set username on top
        if (users != null) {
            multipleUserMode = true;
            selectedUser.setText("Multiple users selected");
            return;
        }
        filesToSend = new ArrayList<>();
        selectedUser.setText("Selected user: " + user.getlName() + ", " + user.getfName());
    }
}