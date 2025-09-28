package com.example.networkairlines;

public class SendRequest extends Transmission {
    private final String mess;
    private final long size;

    /**
     * Constructor for a SendRequest. This inheritor of the Transmission class is distinguished by a message or
     * description of the package (String) as well as a long value indicating the size of all the files to be sent.
     * @param sender
     * @param receiver
     * @param nature = 1
     * @param id
     * @param mess
     * @param size
     */
    public SendRequest(User sender, User receiver, int nature, int id, String mess, long size) {
        super(sender, receiver, nature, id);
        this.mess = mess;
        this.size = size;
    }

    /**
     * Returns package description.
     * @return String
     */
    public String getMess() {
        return mess;
    }

    /**
     * Returns file package size
     * @return long
     */
    public long getSize() {
        return size;
    }
}