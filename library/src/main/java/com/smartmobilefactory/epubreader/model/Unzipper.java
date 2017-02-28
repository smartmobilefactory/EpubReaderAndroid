package com.smartmobilefactory.epubreader.model;

import android.content.Context;
import android.support.v4.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class Unzipper {

    /**
     * @return (unzipped location, epubfile)
     * @throws IOException
     */
    static Pair<File, File> unzipEpubIfNeeded(Context context, String uri, File destDir) throws IOException {
        File file;
        if (uri.startsWith("file:///android_asset/")) {
            file = copyAsset(context, uri, destDir);
        } else {
            uri = uri.replace("file://", "");
            file = new File(uri);
        }

        File destination = new File(destDir, md5(uri) + file.getName() + ".unzipped/");

        if (destination.exists()) {
            return new Pair<>(destination, file);
        }

        try {
            unzip(file, destination);
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            destination.delete();
            throw e;
        }

        return new Pair<>(destination, file);
    }

    private static File copyAsset(Context context, String uri, File destDir) throws IOException {
        File file = new File(uri);
        File localFile = new File(destDir, file.getName());
        if (localFile.exists()) {
            return localFile;
        }
        localFile.createNewFile();
        try {
            InputStream steam = context.getAssets().open(uri.replace("file:///android_asset/", ""));
            copyFile(steam, new FileOutputStream(localFile));
            return localFile;
        } catch (IOException e) {
            e.printStackTrace();
            localFile.delete();
        }
        return file;
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.close();
    }

    private static void unzip(File file, File targetDirectory) throws IOException {

        ZipFile zipfile = new ZipFile(file);
        Enumeration e = zipfile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            unzipEntry(zipfile, entry, targetDirectory.getAbsolutePath());
        }
        zipfile.close();

    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry,
                                   String outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            byte[] buffer = new byte[1024];
            int i;
            while ((i = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, i);
            }
            outputStream.flush();
        } finally {
            closeSilent(outputStream);
            closeSilent(inputStream);
        }

    }

    private static void createDir(File dir) {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void closeSilent(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            // ignore
        }
    }
}
