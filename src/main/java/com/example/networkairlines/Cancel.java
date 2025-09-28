package com.example.networkairlines;

public class Cancel extends Transmission {
    /**
     * Constructor for an object aimed to cancel the
     * transfer of a file package. It is distinguished by
     * not having any additional instance variables, and
     * by a 'nature' value of 3.
     * @param sender
     * @param receiver
     * @param nature = 3
     * @param id
     */
    public Cancel(User sender, User receiver, int nature, int id) {
        super(sender, receiver, nature, id);
    }
}