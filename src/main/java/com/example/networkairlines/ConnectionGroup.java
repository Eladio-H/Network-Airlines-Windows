package com.example.networkairlines;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;

public class ConnectionGroup {
    private final ArrayList<User> connectedUsers = new ArrayList<>();
    private final String[] possIP;
    private final int port1;
    private final int port2;
    private int inUsePinging = -1;
    private boolean receptionFinished = true;
    private final Object receptionLock = new Object();
    private final boolean receiving;

    /**
     * Constructor which starts running pings and possibly receiving based on the object's instance variables.
     * @param possIP
     * @param port1
     * @param port2
     * @param receiving
     */
    public ConnectionGroup(String[] possIP, int port1, int port2, boolean receiving){
        //Sets instance variables
        this.possIP = possIP;
        this.port1 = port1;
        this.port2 = port2;
        this.receiving = receiving;
        //Runs connections
        tryConnections();
    }

    /**
     * Returns all the scanned users on the network
     * @return ArrayList object of User objects
     */
    public ArrayList<User> getConnectedUsers(){
        return connectedUsers;
    }

    /**
     * Runs every time a user is added. Checks if there is a duplicate and eliminates it if there is.
     * @param user
     */
    private void eliminateDuplicate(User user) {
        //Loops through all the connected users
        for (int i = connectedUsers.size()-2; i >= 0; i--){
            User possibleDuplicate = connectedUsers.get(i);
            //Compares MAC addresses
            if (user.getMacAddress().equals(possibleDuplicate.getMacAddress())){
                //If they are the same, the user is the same so the duplicate is eliminted.
                connectedUsers.remove(i);
                break;
            }
        }
    }

    /**
     * A function which checks the possible IPv4 addresses
     * on the network for connected users
     * @throws IOException
     */
    private void ping() throws IOException {
        if (!StartController.isConnectedToInternet()) {
            /*
            Exception handling: quits function if not connected to Internet
            Runs function which notifies user and goes back to main menu
             */
            ConnectionsManager.setConnected(false);
            ConnectionsManager.connectionEnded(ConnectionsManager.class);
            return;
        }
        //Chooses randomly between the two ports to ping from
        double randomDouble = Math.random();
        int choiceOfPort;
        if (randomDouble < 0.5){
            choiceOfPort = port1;
            inUsePinging = port1;
        }else{
            choiceOfPort = port2;
            inUsePinging = port2;
        }
        /*
        Waits for the reception function to finish because it
        could be potentially using the same port that was chosen
         */
        synchronized (receptionLock){
            while (!receptionFinished){
                try{
                    receptionLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //Loops for possible IPv4 addresses
        for (String ipAddress : possIP) {
            if (ipAddress == null){
                continue;
            }
            try {
                Socket socket = new Socket();
                try {
                    //Attempts connection
                    socket.connect(new InetSocketAddress(ipAddress, choiceOfPort), 1000);
                } catch (SocketTimeoutException | SocketException e) {
                    //If it doesn't connect in 1 second move on to another IP
                    continue;
                }
                /*
                At this point in the code the connection worked. Now it
                receives the user information object through the
                ObjectInputStream.
                 */
                ObjectInputStream ois;
                try {
                    ois = new ObjectInputStream(socket.getInputStream());
                } catch (Exception e) {
                    continue;
                }
                User user = (User) ois.readObject();
                ois.close();
                socket.close();
                //Adds received user object to connected user
                connectedUsers.add(user);
                eliminateDuplicate(user);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (BindException ignored) {}
        }
    }

    /**
     * Receives from opposite port that the ping function is using
     * in order to prevent clashes and errors.
     * @throws IOException
     */
    private void receivePing() throws IOException {
        Socket socket;
        int choiceOfPort;
        if (inUsePinging == port1) {
            choiceOfPort = port2;
        } else {
            choiceOfPort = port1;
        }
        /*
        From this point another ping function is not allowed to start
        Because it could potentially run pings from the same receiving port
         */
        synchronized (receptionLock) {
            receptionFinished = false;
            receptionLock.notifyAll();
        }
        ServerSocket server = new ServerSocket(choiceOfPort);
        server.setSoTimeout(1000);
        try {
            //Waits for any ping for 1 second
            socket = server.accept();
        } catch (Exception e) {
            //No connection was detected on this run
            server.close();
            synchronized (receptionLock) {
                receptionFinished = true;
                receptionLock.notifyAll();
            }
            return;
        }
        /*
        At this point a connection is made.
        Local user's information is sent over the OutputStream.
         */
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        User localUser = new User(LocalUser.getIpAddress(), LocalUser.getMacAddress(), LocalUser.getfName(), LocalUser.getlName(), LocalUser.getDepartment(), LocalUser.getOrgName(), LocalUser.getReceivePort(), LocalUser.getBackup(), LocalUser.getPublicKey());
        oos.writeObject(localUser);
        //Now the other user has received the local user's info
        oos.close();
        socket.close();
        server.close();
        synchronized (receptionLock) {
            receptionFinished = true;
            receptionLock.notifyAll();
        }
    }

    /**
     * This function is run from the constructor of
     * the ConnectionGroup class and uses the
     * functions to ping and possibly receive.
     */
    private void tryConnections() {
        Thread toPing = new Thread(() -> {
            //Ping while connected to the internet.
            while (ConnectionsManager.isConnected()) {
                try {
                    ping();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Thread toReceive = new Thread(() -> {
            //Receive while connected to the internet.
            while (ConnectionsManager.isConnected()){
                try {
                    receivePing();
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        toPing.start();
        /*
        Receive if user belongs to this object's allocated
        group of IP addresses.
         */
        if (receiving) {
            toReceive.start();
        }
    }
}