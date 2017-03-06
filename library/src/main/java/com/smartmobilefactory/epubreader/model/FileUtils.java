package com.smartmobilefactory.epubreader.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * http://grepcode.com/file_/repo1.maven.org/maven2/commons-io/commons-io/1.1/org/apache/commons/io/FileUtils.java/?v=source
 */
class FileUtils {

    public static void tryDeleteDirectory(File directory)
            throws IOException {
        try {
            deleteDirectory(directory);
        } catch (Exception e){}
    }

    /**
     * Recursively delete a directory.
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory)
            throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);
        if (!directory.delete()) {
            String message =
                    "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Clean a directory without deleting it.
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * <p>
     * Delete a file. If file is a directory, delete it and all sub-directories.
     * </p>
     * <p>
     * The difference between File.delete() and this method are:
     * </p>
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     *      (java.io.File methods returns a boolean)</li>
     * </ul>
     * @param file file or directory to delete.
     * @throws IOException in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            if (!file.delete()) {
                String message =
                        "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

}
