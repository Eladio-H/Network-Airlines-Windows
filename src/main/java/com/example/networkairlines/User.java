package com.example.networkairlines;

import java.io.Serializable;
import java.security.PublicKey;

public class User implements Serializable {
    private final String ipAddress;
    private final String macAddress;
    private final String fName;
    private final String lName;
    private final String department;
    private final String orgName;
    private final int receivePort;
    private final int backup;
    private final PublicKey publicKey;

    /**
     * Constructor method for each User object.
     * @param ipAddress
     * @param macAddress
     * @param fName
     * @param lName
     * @param department
     * @param orgName
     * @param receivePort
     * @param backup
     * @param publicKey
     */
    public User(String ipAddress, String macAddress, String fName, String lName, String department, String orgName, int receivePort, int backup, PublicKey publicKey) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.fName = fName;
        this.lName = lName;
        this.department = department;
        this.orgName = orgName;
        this.receivePort = receivePort;
        this.backup = backup;
        this.publicKey = publicKey;
    }

    /**
     * Accessor method for user's IP address.
     * @return String
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Accessor method for user's MAC address.
     * @return String
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Accessor method for user's first name.
     * @return String
     */
    public String getfName() {
        return fName;
    }

    /**
     * Accessor method for user's last name.
     * @return String
     */
    public String getlName() {
        return lName;
    }

    /**
     * Accessor method for user's department.
     * @return String
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Accessor method for user's organisation name.
     * @return String
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * Accessor method for user's general Transmission-receiving.
     * @return Integer.
     */
    public int getReceivePort() {
        return receivePort;
    }

    /**
     * Accessor method for user's file-receiving (Package) port.
     * @return Integer
     */
    public int getFileReceivingPort() {
        return backup;
    }

    /**
     * Accessor method for user's Public key.
     * @return Public key object
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
}