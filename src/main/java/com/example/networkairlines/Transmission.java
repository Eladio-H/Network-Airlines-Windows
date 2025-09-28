package com.example.networkairlines;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;

public class Transmission implements Serializable {
    /*
    1: Send request
    2: Response to request
    3: Package
    4: Cancel
     */
    private final int nature;
    private final User sender;
    private final User receiver;
    private final int id;

    /**
     * Transmission constructor
     * @param sender
     * @param receiver
     * @param nature
     * @param id
     */
    public Transmission(User sender, User receiver, int nature, int id) {
        this.sender = sender;
        this.nature = nature; //Used to identify which inheritor it is
        this.receiver = receiver;
        this.id = id;
    }

    /**
     * Accessor method for sender 'User' object.
     * @return User object
     */
    public User getSender() {
        return sender;
    }

    /**
     * Accessor method for receiver 'User' object.
     * @return User object
     */
    public User getReceiver() {
        return receiver;
    }
    public int getID() {
        return id;
    }

    /**
     * Key function run on a certain Transmission object that reads which inheritor
     * it is along with its key data and responds appropriately depending on the situation.
     * @throws IOException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    public void processTransmission() throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (this.isSendRequest()) { //Transmission is a send request; show in Inbox.
            SendRequest sendRequest = (SendRequest) this;
            System.out.println(sendRequest.getMess());
            Inbox.add(sendRequest);
            if (Inbox.getScreenIsOpen()) {
                Inbox.getController().updateInbox();
            }
            ConnectedController.getController().update();
        } else if (this.isResponse()) { //Transmission is a response to a send request; send files if the response is an acceptance.
            System.out.println("Response received");
            Response response = (Response) this;
            boolean send = response.getResponse();
            Package filePackage = Outbox.find(response);
            Outbox.remove(response);
            if (Outbox.getScreenIsOpen()) {
                Outbox.getController().updateOutbox();
            }
            if (!send) {
                //Receiver has NOT accepted the SendRequest.
                return;
            }
            User receiver = response.getReceiver();
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(receiver.getIpAddress(), receiver.getFileReceivingPort()));
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            assert filePackage != null;
            byte[][] packageData = filePackage.encrypt(receiver.getPublicKey());
            //Send total number of chunks
            int totalChunks = packageData.length;
            oos.writeInt(totalChunks);
            //Send chunks one by one
            for (int i = 0; i < totalChunks; i++) {
                byte[] chunk = packageData[i];
                oos.writeObject(chunk);
            }
            oos.flush();
            oos.close();
            socket.close();
            System.out.println("Package sent");
            ConnectedController.getController().update();
        } else if (this.isCancel()) {
            //Cancel transmission, eliminate send request from inbox
            Cancel cancelCommand = (Cancel) this;
            Inbox.remove(cancelCommand);
            if (Inbox.getScreenIsOpen()) {
                Inbox.getController().updateInbox();
            }
            ConnectedController.getController().update();
        }
    }

    /**
     * Is it a send request?
     * @return boolean
     */
    public boolean isSendRequest() {
        return nature == 1;
    }

    /**
     * Is it a response to a send request?
     * @return boolean
     */
    public boolean isResponse() {
        return nature == 2;
    }

    /**
     * Is it a request cancellation?
     * @return boolean
     */
    public boolean isCancel() {
        return nature == 3;
    }

    /**
     * Translates a Transmission object to its corresponding byte array.
     * @return byte array
     */
    public byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray = bos.toByteArray();
        return byteArray;
    }

    /**
     * Static method that translates a byte array corresponding to a Transmission object into the actual object.
     * @param data
     * @return Transmission object.
     */
    public static Transmission fromByteArray(byte[] data) {
        Transmission transmission = null;
        try {
            //Reads the bytes as a stream.
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            transmission = (Transmission) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return transmission;
    }

    /**
     * Takes a transmission, splits its byte array up into smaller chunks that can each be encrypted,
     * and finally returns a 2D array of all the encrypted chunks.
     * @param publicKey is taken from LocalUser class
     * @return 2D byte array of encrypted chunks
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[][] encrypt(PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        //Enables framework to encrypt through public key
        byte[] notCiphered = toByteArray();
        byte[] cipherTemp;
        //Finds number of chunks (117 bytes) within the whole byte array
        int numChunks = notCiphered.length / 117 + 1;
        if (notCiphered.length % 117 == 0) {
            numChunks--;
        }
        byte[][] chunks = new byte[numChunks][];
        int bytesEncrypted = 0;
        int chunksEncrypted = 0;
        while (bytesEncrypted < notCiphered.length) {
            cipherTemp = new byte[117];
            for (int i = 0; i < 117; i++) {
                if (bytesEncrypted >= notCiphered.length) {
                    break;
                }
                bytesEncrypted++;
                cipherTemp[i] = notCiphered[bytesEncrypted-1];
            }
            cipher.update(cipherTemp);
            byte[] cipheredChunk = cipher.doFinal();
            chunks[chunksEncrypted] = cipheredChunk;
            chunksEncrypted++;
        }
        return chunks;
    }

    /**
     * Simple and less computationally demanding method to decrypt a byte array representing a transmission, but only
     * used for Transmissions which are sure to have relatively small byte arrays (the transmissions that do not
     * represent file packages).
     * @param ciphered
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static Transmission decrypt(byte[][] ciphered) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, LocalUser.getPrivateKey());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < ciphered.length; i++) {
            byte[] encryptedChunk = ciphered[i];
            cipher.update(encryptedChunk);
            byte[] decryptedChunk = cipher.doFinal();
            outputStream.write(decryptedChunk, 0, decryptedChunk.length);
        }
        byte[] decryptedData = outputStream.toByteArray();
        Transmission transmission = fromByteArray(decryptedData);
        return transmission;
    }
    public void encrypt(PublicKey publicKey, int indicator) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] notCiphered = toByteArray();
        int numChunks = notCiphered.length/117 + 1;
        if (notCiphered.length % 117 == 0) {
            numChunks--;
        }
        int length = numChunks / 4 + 1;
        if (numChunks % 4 == 0) {
            length--;
        }
        byte[][][] chunks = new byte[4][length][];
        int bytesAssigned = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < length; j++) {
                if (i*j >= numChunks) {
                    break;
                }
                byte[] chunk = new byte[117];
                for (int k = 0; k < 117; k++) {
                    if (bytesAssigned >= notCiphered.length) {
                        break;
                    }
                    chunk[k] = notCiphered[bytesAssigned];
                    bytesAssigned++;
                }
                chunks[i][j] = chunk;
            }
        }
        ArrayList<byte[][]> bytes = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            encryptChunk(i, bytes, cipher, chunks[i], numChunks);
        }
    }
    private static void encryptChunk(int index, ArrayList<byte[][]> bytes, Cipher cipher, byte[][] correspondance, int numChunks) {
        Thread start = new Thread(() -> {
            byte[][] encrypted = new byte[correspondance.length][];
            for (int i = 0; i < correspondance.length; i++) {
                byte[] encryptedChunk = new byte[0];
                try {
                    encryptedChunk = cipher.doFinal(encryptedChunk);
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                }
                encrypted[i] = encryptedChunk;
            }
            bytes.set(index, encrypted);
            boolean control = true;
            for (byte[][] bArray : bytes) {
                if (bArray == null) {
                    control = false;
                    break;
                }
            }
            if (control) {
                byte[][] encryptedFinal = new byte[numChunks][];
                int chunksAssigned = 0;
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < bytes.get(i).length; j++) {
                        encryptedFinal[chunksAssigned] = bytes.get(i)[j];
                        chunksAssigned++;
                    }
                }
                //Return this 2D array somehow
                System.out.println("Package encrypted");
            }
        });
        start.start();
    }

    /**
     * Rapidly decrypts a potentially large Transmission (containing a file package) using multi-threading.
     * @param ciphered is the array of chunks to be decrypted.
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static void decryptAndSave(byte[][] ciphered) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Chunks is split up into groups of length 100 (3D array):
        int length = (int) (Math.ceil(ciphered.length / 100.0));
        byte[][][] correspondence = new byte[100][length][];
        //Fills the 3D array
        int numChunks = ciphered.length;
        int chunksAssigned = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < length; j++) {
                if (chunksAssigned == numChunks) {
                    break;
                }
                correspondence[i][j] = ciphered[chunksAssigned];
                chunksAssigned++;
            }
        }
        //'bytes' will be the array of each decrypted group
        ArrayList<byte[]> bytes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            bytes.add(null);
        }
        /*
        Decrypts each group. All the decrypted chunks in a group
        End up in the same array
         */
        for (int i = 0; i < 100; i++) {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, LocalUser.getPrivateKey());
            decryptChunk(i, bytes, cipher, correspondence[i]);
        }
    }

    /**
     * Runs a thread on a group of chunks that decrypts them all and helps rebuild some of the Transmission's
     * byte array. This method is only run for Transmissions that are file Packages because of their large size
     * and need of faster decryption using multi-threading. If it rebuilds the Transmission's last chunk, then
     * the data is saved as a file.
     * @param index specifies which part of the array 'bytes' to save the
     *              decrypted chunks
     * @param bytes is the main array of bytes that is to be filled (in the right indices
     *              with the bytes of the decrypted chunks).
     * @param cipher enables to decrypt each chunk using the private key stored in a
     *               static variable in the LocalUser class.
     * @param correspondance provides the array of chunks (byte arrays) that
     *                       are to be decrypted.
     */
    private static void decryptChunk(int index, ArrayList<byte[]> bytes, Cipher cipher, byte[][] correspondance) {
        Thread start = new Thread(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (int i = 0; i < correspondance.length; i++) {
                byte[] encryptedChunk = correspondance[i];
                if (encryptedChunk == null) {
                    break;
                }
                byte[] decryptedChunk = new byte[0];
                try {
                    cipher.update(encryptedChunk);
                    decryptedChunk = cipher.doFinal();
                } catch (Exception e) {
                    System.out.println(encryptedChunk.length);
                    e.printStackTrace();
                }
                outputStream.write(decryptedChunk, 0, decryptedChunk.length);
            }
            byte[] decryptedChunk = outputStream.toByteArray();
            //Sets the decrypted chunks to the right position in 'bytes'
            bytes.set(index, decryptedChunk);
            //Checks if all the bytes have been decrypted
            boolean control = true;
            for (byte[] aByte : bytes) {
                if (aByte == null) {
                    control = false;
                    break;
                }
            }
            //If all are decrypted, then the data can be saved as a file.
            if (control) {
                //Write all the decrypted chunks to one large byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                for (byte[] bArray : bytes) {
                    try {
                        bos.write(bArray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                byte[] decryptedData = bos.toByteArray();
                //Obtain and save it as a file Package
                Package filePackage = (Package) fromByteArray(decryptedData);
                ArrayList<TrackedFile> files = filePackage.getFiles();
                for (TrackedFile tf : files) {
                    try {
                        /*
                        A function I wrote to save a file to directory
                        specified in the settings of the app.
                         */
                        TrackedFile.writeToFile(tf);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Package received and decrypted");
            }
        });
        start.start();
    }
}