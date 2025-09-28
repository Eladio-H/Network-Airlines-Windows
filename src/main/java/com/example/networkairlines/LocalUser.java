package com.example.networkairlines;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.*;
import java.util.Enumeration;
import java.util.Scanner;

public class LocalUser {
    private static String fName;
    private static String lName;
    private static String orgName = "";
    private static String department = "";
    private static File directory;
    private static String macAddress;
    private static String ipAddress;
    private static int receivePort;
    private static int backup;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    /**
     * Finds and sets IP address for future use
     * @throws SocketException if user is not connected to the internet
     */
    public static void setIpAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()){
            NetworkInterface ni = interfaces.nextElement();
            if (!ni.isLoopback() && ni.isUp()){
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address){
                        ipAddress = address.getHostAddress();
                        System.out.println(ipAddress);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Finds and sets MAC address for future use
     * @throws IOException if user is not connected to internet
     */
    public static void setMacAddress() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            InetAddress host = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(host);
            byte[] hardwareAddress = ni.getHardwareAddress();
            String[] hexadecimal = new String[hardwareAddress.length];

            for (int i = 0; i < hardwareAddress.length; ++i) {
                hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
            }

            macAddress = String.join("-", hexadecimal);
            System.out.println(macAddress);
        } else if (os.contains("mac")) {
            Process process = Runtime.getRuntime().exec("ifconfig");
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                if (line.contains("ether")){
                    macAddress = line.split("ether")[1].trim().split(" ")[0];
                    System.out.println(macAddress);
                    break;
                }
            }
        }
    }

    /**
     * This function generates a user's private and
     * public keys. The private key is NOT shared.
     * @throws NoSuchAlgorithmException
     */
    public static void setKeys() throws NoSuchAlgorithmException {
        //1024-bit RSA encryption
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair pair = kpg.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();
    }

    /**
     * Returns the port out of the 65000+ the user uses to receive transmissions
     * @return integer
     */
    public static int getReceivePort(){
        return receivePort;
    }

    /**
     * Returns the port the user uses to receive file packages (a specific type of transmission)
     * @return
     */
    public static int getBackup() {
        return backup;
    }

    /**
     * Sets the ports the user will use to receive transmissions and file packages.
     */
    public static void setReceivePorts(){
        int lowerBound = 3257;
        int upperBound = 65500;
        receivePort = (int) (Math.random() * (upperBound - lowerBound + 1)) + lowerBound;
        backup = (int) (Math.random() * (upperBound - lowerBound + 1)) + lowerBound;
        System.out.println(receivePort);
        System.out.println(backup);
    }

    /**
     * Returns the user's first name
     * @return String
     */
    public static String getfName() {
        return fName;
    }

    /**
     * Sets user's first name (run from saving certain settings)
     * @param fName String
     */
    public static void setfName(String fName) {
        LocalUser.fName = fName;
    }

    /**
     * Returns user's last name
     * @return String
     */
    public static String getlName() {
        return lName;
    }

    /**
     * Sets user's last name (run from saving certain settings)
     * @param lName String
     */
    public static void setlName(String lName) {
        LocalUser.lName = lName;
    }

    /**
     * Returns user's IP address in String form
     * @return String
     */
    public static String getIpAddress() {
        return ipAddress;
    }

    /**
     * Returns user's MAC address in String form
     * @return String
     */
    public static String getMacAddress(){
        return macAddress;
    }

    /**
     * Returns user's public key
     * @return PublicKey object
     */
    public static PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Returns user's private key
     * @return PrivateKey object
     */
    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Returns organisation name
     * @return String
     */
    public static String getOrgName() {
        return orgName;
    }

    /**
     * Sets organisation name (run from saving certain settings)
     * @param orgName String
     */
    public static void setOrgName(String orgName) {
        LocalUser.orgName = orgName;
    }

    /**
     * Returns department name
     * @return String
     */
    public static String getDepartment() {
        return department;
    }

    /**
     * Sets department name (run from saving certain settings)
     * @param department String
     */
    public static void setDepartment(String department) {
        LocalUser.department = department;
    }

    /**
     * Returns directory user has chosen for where received files are saved
     * @return File object
     */
    public static File getDirectory(){
        return directory;
    }

    /**
     * Sets directory user has chosen for where received files are saved (run from saving certain settings)
     * @param directory File object
     */
    public static void setDirectory(File directory){
        LocalUser.directory = directory;
    }
}