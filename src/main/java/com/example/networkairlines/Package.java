package com.example.networkairlines;

import java.util.ArrayList;

public class Package extends Transmission {
    private final ArrayList<TrackedFile> files;
    private final ArrayList<User> receivers;
    private int numReceived = 0;

    /**
     * Constructor for Package object. It is quite simple code considering it inherits from the Transmission class.
     * @param sender
     * @param receiver only one receiver
     * @param nature = 1
     * @param id
     * @param files
     */
    public Package(User sender, User receiver, int nature, int id, ArrayList<TrackedFile> files) {
        super(sender, receiver, nature, id);
        this.files = files;
        this.receivers = null;
    }

    /**
     * Overload the constructor to allow cases where the user is sending to multiple receivers.
     * @param sender
     * @param receivers multiple receivers
     * @param nature = 1
     * @param id
     * @param files
     */
    public Package(User sender, ArrayList<User> receivers, int nature, int id, ArrayList<TrackedFile> files) {
        super(sender, null, nature, id);
        this.files = files;
        this.receivers = receivers;
    }

    /**
     * Returns all the files that are to be sent as TrackedFile objects (more information attached to them such as
     * their relative path and the byte array representing the file.
     * @return ArrayList object of TrackedFile objects
     */
    public ArrayList<TrackedFile> getFiles() {
        return files;
    }

    /**
     * Returns the number of bytes the file is made of.
     * @return long
     */
    public long getSize() {
        long bytes = 0;
        for (TrackedFile trackedFile : files) {
            bytes += trackedFile.getFileData().length;
        }
        return bytes;
    }

    /**
     * For cases where there are multiple receivers, this returns all the receivers as User objects.
     * @return ArrayList of User objects.
     */
    public ArrayList<User> getReceivers() {
        return receivers;
    }

    /**
     * When one of the receivers interacts with the send request sent to them (accepts/rejects it), this function adds
     * to the counter of users which have done so. When all the users have done so, this function also removes the
     * Package from the Outbox.
     */
    public void addReceived() {
        numReceived++;
        if (numReceived == receivers.size()) {
            Outbox.remove(this);
            if (Outbox.getScreenIsOpen()) {
                Outbox.getController().updateOutbox();
            }
            ConnectedController.getController().update();
        }
    }
}