package com.example.networkairlines;

import java.io.*;

public class TrackedFile implements Serializable {
    private final String path;
    private final File file;
    private byte[] fileData;
    private final String fileName;

    /**
     * TrackedFile constructor class using a file to be sent and its relative path. This allows for the build-up of
     * a TrackedFile object which saves the file data as a byte array as well as the file's name.
     * @param path
     * @param file
     * @throws IOException
     */
    public TrackedFile(String path, File file) throws IOException {
        this.path = path;
        this.file = file;
        this.fileName = file.getName();
        setFileData();
    }

    /**
     * Sets the file data (constructs the file's byte array) so that the file's data is no longer dependent on having
     * the reference to the file on the user's computer.
     * @throws IOException
     */
    private void setFileData() throws IOException {
        DataInputStream diStream = new DataInputStream(new FileInputStream(file));
        long numBytes = (int) file.length();
        //Ensure the length of the array is equal or below INTEGER.MAX_VALUE
        fileData = new byte[(int) numBytes];
        int bytesRead = 0;
        int bytesReadPerIteration = 0;
        while (bytesRead < fileData.length) {
            bytesReadPerIteration = diStream.read(fileData, bytesRead, fileData.length-bytesRead);
            if (!(bytesReadPerIteration > 0)){
                break;
            }
            numBytes += bytesReadPerIteration;
        }
    }

    /**
     * Returns relative path of file
     * @return String
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns file name
     * @return String
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns file data
     * @return byte array
     */
    public byte[] getFileData() {
        return fileData;
    }

    /**
     * Static function which takes a TrackedFile object and, through the info on where a receiver has chosen to save its
     * received files as well as the relative path of this file, saves it in some directory in the receiver's computer.
     * @param trackedFile
     * @throws IOException
     */
    public static void writeToFile(TrackedFile trackedFile) throws IOException {
        String path = LocalUser.getDirectory().getAbsolutePath() + File.separator;
        for (int i = 0; i < trackedFile.getPath().length(); i++) {
            if (trackedFile.getPath().charAt(i) == '/' || trackedFile.getPath().charAt(i) == '\\') {
                path += File.separator;
                continue;
            }
            path += trackedFile.getPath().charAt(i);
        }
        path += File.separator + trackedFile.getFileName();
        File file = new File(path);
        //Handles FileNotFoundException
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(trackedFile.getFileData());
        fos.close();
    }
}