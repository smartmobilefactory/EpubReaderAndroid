package com.smartmobilefactory.epubreader.model;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nl.siegmann.epublib.domain.Book;

class EpubStorageHelper {

    static final String ANDROID_ASSETS = "file:///android_asset/";

    static File getEpubReaderCacheDir(Context context) {
        File cacheDir = new File(context.getCacheDir(), "epubreader_cache");
        cacheDir.mkdirs();
        return cacheDir;
    }

    static File getOpfPath(Epub epub) {
        return getOpfPath(epub.getLocation());
    }

    static File getOpfPath(File folder) {
        String relativeOpfPath = "";
        try {
            // get the OPF path, directly from container.xml

            BufferedReader br
                    = new BufferedReader(new InputStreamReader(new FileInputStream(folder
                    + "/META-INF/container.xml"), "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                //if (line.indexOf(getS(R.string.full_path)) > -1)
                if (line.contains("full-path")) {
                    int start = line.indexOf("full-path");
                    //int start2 = line.indexOf("\"", start);
                    int start2 = line.indexOf('\"', start);
                    int stop2 = line.indexOf('\"', start2 + 1);
                    if (start2 > -1 && stop2 > start2) {
                        relativeOpfPath = line.substring(start2 + 1, stop2).trim();
                        break;
                    }
                }
            }
            br.close();

            // in case the OPF file is in the root directory
            if (!relativeOpfPath.contains("/")) {
                return folder;
            }

            // remove the OPF file name and the preceding '/'
            int last = relativeOpfPath.lastIndexOf('/');
            if (last > -1) {
                relativeOpfPath = relativeOpfPath.substring(0, last);
            }

            return new File(folder, relativeOpfPath);
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
        return folder;
    }

    static Epub fromUri(Context context, String uri) throws IOException {
        File cacheDir = getEpubReaderCacheDir(context);
        File unzippedEpubLocation = Unzipper.unzipEpubIfNeeded(context, uri, cacheDir);
        try {
            Book book = UncompressedEpubReader.readUncompressedBook(unzippedEpubLocation);
            return new Epub(book, unzippedEpubLocation);
        } finally {
            System.gc();
        }
    }

    static Epub fromFolder(Context context, File folder) throws IOException {
        try {
            Book book = UncompressedEpubReader.readUncompressedBook(folder);
            return new Epub(book, folder);
        } finally {
            System.gc();
        }
    }

    static InputStream openFromUri(Context context, String uriString) throws IOException {

        Uri uri = Uri.parse(uriString);
        InputStream inputStream;
        if (uriString.startsWith(ANDROID_ASSETS)) {
            inputStream = context.getAssets().open(uriString.replace(ANDROID_ASSETS, ""));
        } else {
            inputStream = context.getContentResolver().openInputStream(uri);
        }
        return inputStream;
    }
}
