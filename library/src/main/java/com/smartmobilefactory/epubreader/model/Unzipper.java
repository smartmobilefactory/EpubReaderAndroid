package com.smartmobilefactory.epubreader.model;

import android.content.Context;
import android.support.v4.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

class Unzipper {

    static File getEpubCacheFolder(File destDir, String uri) {
        return new File(destDir, md5(uri));
    }

    /**
     * @return (unzipped location, epubfile)
     * @throws IOException
     */
    static File unzipEpubIfNeeded(Context context, String uri, File destDir) throws IOException {
        InputStream inputStream = EpubStorageHelper.openFromUri(context, uri);
        File destination = getEpubCacheFolder(destDir, uri);

        if (destination.exists()) {
            File ready = new File(destDir, ".ready");
            if (ready.exists()) {
                return destination;
            }
        }

        try {
            unzip(inputStream, destination);
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            try {
                FileUtils.deleteDirectory(destination);
            } catch (Exception ignore) {}

            throw e;
        }

        return destination;
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

    private static void unzip(InputStream inputStream, File folder) throws IOException {

        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            FileUtils.deleteDirectory(folder);
        }

        ZipInputStream zis;

        byte[] buffer = new byte[2048];

        try {
            String filename;
            zis = new ZipInputStream(inputStream);

            ZipEntry ze;
            int count;
            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                File file = new File(folder, filename);

                // make directory if necessary
                new File(file.getParent()).mkdirs();

                if (!ze.isDirectory() && !file.isDirectory()) {
                    FileOutputStream fout = new FileOutputStream(file);
                    while ((count = zis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                    fout.close();
                }
                zis.closeEntry();
            }

            inputStream.close();
            zis.close();

            //file to show that everything is fully unzipped
            File ready = new File(folder, ".ready");
            if (!ready.exists()) {
                ready.createNewFile();
            }

        } catch (IOException e) {
            FileUtils.tryDeleteDirectory(folder);
            throw e;
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
