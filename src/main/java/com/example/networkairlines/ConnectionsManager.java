package com.example.networkairlines;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

public class ConnectionsManager {
    private static boolean connected = false;
    private static ArrayList<User> connectedUsers = new ArrayList<>();
    private static String[][] possIP = new String[128][4];
    private static int indexForReceive;
    private static boolean connectionFailed = false;
    public static void setConnected(boolean condition) {
        connected = condition;
    }
    public static boolean isConnected() {
        return connected;
    }
    public static ArrayList<User> getConnectedUsers() {
        return connectedUsers;
    }
    public static void setConnectionFailed(boolean b) {
        ConnectionsManager.connectionFailed = b;
    }

    /**
     * Sets all the possible IP addresses using the user's IPv4 address
     * @throws SocketException if user is not connected to the internet but at this point this check has already been done
     */
    public static void setPossIP() throws SocketException {
        //Finds user's IPv4 address
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        String ipAddress = "";
        while (interfaces.hasMoreElements()){
            NetworkInterface ni = interfaces.nextElement();
            if (!ni.isLoopback() && ni.isUp()){
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address){
                        ipAddress = address.getHostAddress();
                        break;
                    }
                }
            }
        }
        //Constructs 2D array possIP based on ipAddress
        String[] bytes = ipAddress.split("\\.");
        String base = "";
        for (int i = 0; i < 3; i++){
            base += bytes[i] + ".";
        }
        indexForReceive = (Integer.parseInt(bytes[3])/4);
        int n = 0;
        String[][] arr1 = new String[64][4];
        String[] temp = new String[4];
        for (int i = 1; i <= 256; i++){
            if ((i+4) % 4 == 0){
                arr1[n] = temp;
                temp = new String[4];
                n++;
            }
            //Avoid pinging this user
            if ((base+i).equals(ipAddress)){
                continue;
            }
            temp[(i+4) % 4] = base+i;
        }
        int num = Integer.parseInt(bytes[2]);
        if (num == 0) {
            num = 1;
        } else if (num == 1) {
            num = 0;
            indexForReceive += 64;
        } else {
            possIP = arr1;
            for (int i = 0; i < possIP.length; i++) {
                System.out.println(Arrays.toString(possIP[i]));
            }
            return;
        }
        String base2 = bytes[0] + "." + bytes[1] + "." + num + ".";
        String[][] arr2 = new String[64][4];
        String[] temp2 = new String[4];
        int m = 0;
        for (int i = 1; i <= 256; i++){
            if ((i+4) % 4 == 0) {
                arr2[m] = temp2;
                temp2 = new String[4];
                m++;
            }
            temp2[(i+4) % 4] = base2+i;
        }
        if (num == 0) {
            for (int i = 0; i < 64; i++) {
                possIP[i] = arr2[i];
            }
            int j = 0;
            for (int i = 64; i < 128; i++) {
                possIP[i] = arr1[j];
                j++;
            }
        } else if (num == 1) {
            for (int i = 0; i < 64; i++) {
                possIP[i] = arr1[i];
            }
            int j = 0;
            for (int i = 64; i < 128; i++) {
                possIP[i] = arr2[j];
                j++;
            }
        }
        for (int i = 0; i < possIP.length; i++) {
            System.out.println(Arrays.toString(possIP[i]));
        }
    }

    /**
     * Sorts users alphabetically and according to what has been searched before displaying them on the main screen
     * @param arr the connected users array
     * @return sorted ArrayList object
     */
    public static ArrayList<User> sort(ArrayList<User> arr){
        ArrayList<User> toModify = (ArrayList<User>) arr.clone();
        //Reduces array to only searched content
        String searched;
        try {
            searched = ConnectedController.getSearchText();
        } catch (Exception e) {
            //System.out.println("Error");
            return mergeSort(toModify,0,toModify.size()-1);
        }
        if (!searched.equals("")) {
            for (int i = 0; i < toModify.size(); i++) {
                User user = toModify.get(i);
                if (!(user.getfName().contains(searched) || user.getlName().contains(searched))) {
                    toModify.remove(i);
                    i--;
                }
            }
        }
        //Sorts the remaining array through the recursive merge sort
        return mergeSort(toModify,0,toModify.size()-1);
    }

    /**
     * Merge Sort recursive algorithm which sorts from
     * the leftBound to the rightBound using the 'merge'
     * function
     * @param arr connected users array
     * @param leftBound
     * @param rightBound
     * @return
     */
    public static ArrayList<User> mergeSort(ArrayList<User> arr, int leftBound, int rightBound) {
        ArrayList<User> mod = (ArrayList<User>) arr.clone();
        if (leftBound < rightBound) {
            //Finds the middle point
            int center = leftBound + (rightBound - leftBound) / 2;

            //Sorts first and second halves
            mergeSort(mod, leftBound, center);
            mergeSort(mod, center + 1, rightBound);

            //Merges the sorted halves
            merge(mod, leftBound, center, rightBound);
        }
        return mod;
    }

    /**
     * Merge function which serves the mergeSort algorithm
     * @param arr
     * @param leftBound
     * @param center
     * @param rightBound
     */
    public static void merge(ArrayList<User> arr, int leftBound, int center, int rightBound) {
        // Find sizes of two subarrays to be merged
        int n1 = center - leftBound + 1;
        int n2 = rightBound - center;

        //Creates temporary arrays
        ArrayList<User> left = new ArrayList<>();
        ArrayList<User> right = new ArrayList<>();

        //Copies data to temp arrays
        for (int i = 0; i < n1; ++i) {
            left.add(arr.get(leftBound + i));
        }
        for (int i = 0; i < n2; ++i) {
            right.add(arr.get(center+1 + i));
        }
        //Merge the temp arrays

        //Initial indices of first and second subarrays
        int leftIndex = 0, rightIndex = 0;

        //Initial index of merged subarray array
        int overallIndex = leftBound;

        while (leftIndex < n1 && rightIndex < n2) {
            String lName = left.get(leftIndex).getlName() + " " + left.get(leftIndex).getfName();
            String rName = right.get(rightIndex).getlName() + " " + right.get(rightIndex).getfName();
            int result = lName.compareTo(rName);
            if (result < 0) {
                arr.set(overallIndex, left.get(leftIndex));
                leftIndex++;
            }
            else {
                arr.set(overallIndex, right.get(rightIndex));
                rightIndex++;
            }
            overallIndex++;
        }

        // Copy remaining elements of 'left' if any
        while (leftIndex < n1) {
            arr.set(overallIndex, left.get(leftIndex));
            leftIndex++;
            overallIndex++;
        }

        // Copy remaining elements of 'right' if any
        while (rightIndex < n2) {
            arr.set(overallIndex, right.get(rightIndex));
            rightIndex++;
            overallIndex++;
        }
    }
    
    /**
     * Called within a thread. If it is called for the first
     * time some code runs. Otherwise, it has already been
     * called, so it automatically ends.
     * @param c
     * @throws IOException
     */
    public static void connectionEnded(Class c) throws IOException {
        //Variable starts as false but is changed immediately
        if (connectionFailed) {
            return;
        }
        connectionFailed = true;
        connected = false;
        /*
        Notifies user of connection loss through a pop-up
        notification screen. Here the screen is set up.
         */
        FXMLLoader loader = new FXMLLoader(StartController.class.getResource("notification.fxml"));
        Scene scene = new Scene(loader.load());
        NotificationController controller = loader.getController();
        //Screen acts differently depending on source class.
        controller.setSource(c);
        controller.setText("It seems your internet connection ended, \n please connect again.");
        //Avoid IllegalStateException
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setTitle("Connection lost");
            stage.setScene(scene);
            stage.setOnCloseRequest(event -> {
                //Take user back to start screen.
                try {
                    ChangeScene.change(ConnectedController.getController().getScreen(), "start.fxml");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            stage.show();
            //Disable current screen
            ConnectedController.getController().getScreen().setDisable(true);
        });
    }

    /**
     * This function runs automatically when the user 'starts'
     * the file-sharing program by clicking on 'Start'
     */
    public static void startConnections() {
        /*
        Stores each object that runs pings on each
        different group of IP addresses.
         */
        ConnectionGroup[] connections = new ConnectionGroup[possIP.length];
        for (int i = 0; i < possIP.length; i++){
            /*
            PossIP is a 2D array grouping all possible IP addresses
            in groups of 4
             */
            int port1 = 2500 + 2*i;
            int port2 = 2501 + 2*i;
            String[] possIPs = possIP[i];
            boolean receiving = i == indexForReceive;
            /*
            Assigns 2 ports and 4 possible IP addresses to the
            ping-running object (ConnectionGroup)
             */
            ConnectionGroup connection = new ConnectionGroup(possIPs, port1, port2, receiving);
            connections[i] = connection;
        }
        Thread connect = new Thread(() -> {
            while (connected) {
                //Collects all scanned users from all the objects
                ArrayList<User> connectedUsersTemp = new ArrayList<>();
                for (ConnectionGroup connection : connections) {
                    ArrayList<User> partialConnected = connection.getConnectedUsers();
                    connectedUsersTemp.addAll(partialConnected);
                }
                connectedUsers = connectedUsersTemp;
                //Trigger a GUI update updating the screen showing users
                if (ConnectedController.getScreenIsOpen()) {
                    ConnectedController.getController().updateViewList();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        Thread receiveTransmissions = new Thread(() -> {
            ServerSocket receiveServer;
            /*
            Waits for a user to send a transmission like a
            sending request or a file package.
             */
            while (connected) {
                Socket socket;
                ObjectInputStream ois;
                try {
                    receiveServer = new ServerSocket(LocalUser.getReceivePort());
                    //This line waits for a connection
                    socket = receiveServer.accept();
                    /*
                    At this point a user has connected to this user
                    and is to send a transmission.
                     */
                    ois = new ObjectInputStream(socket.getInputStream());
                    byte[][] encryptedData = (byte[][]) ois.readObject();
                    ois.close();
                    socket.close();
                    receiveServer.close();
                    //The encrypted data is received and is to be decrypted.
                    Transmission decryptedTransmission = Transmission.decrypt(encryptedData);
                    decryptedTransmission.processTransmission();
                    System.out.println("TRANSMISSION RECEIVED");
                } catch (IOException | ClassNotFoundException | NoSuchPaddingException | NoSuchAlgorithmException |
                         InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                    /*
                    Should never happen as the encryption algorithm
                    selected exists and the bytes are sent properly
                    in chunks of the right size and with padding.
                     */
                }
            }
        });
        connect.start();
        receiveTransmissions.start();
    }
}