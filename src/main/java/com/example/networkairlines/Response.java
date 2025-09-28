package com.example.networkairlines;

public class Response extends Transmission {
    private final boolean response;

    /**
     * Constructor for the Response object which follows a SendRequest. It is distinguished by a boolean instance
     * variable indicating whether the receiver has accepted the request. If a true response is received, then the
     * file Package is sent.
     * @param sender
     * @param receiver
     * @param nature = 2
     * @param id
     * @param response will be either true or false depending on whether the receiver has accepted the request.
     */
    public Response(User sender, User receiver, int nature, int id, boolean response) {
        super(sender, receiver, nature, id);
        this.response = response;
    }

    /**
     * Returns the boolean value of the response.
     * @return boolean
     */
    public boolean getResponse() {
        return response;
    }
}