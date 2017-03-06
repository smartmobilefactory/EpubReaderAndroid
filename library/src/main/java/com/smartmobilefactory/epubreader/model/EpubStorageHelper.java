package com.smartmobilefactory.epubreader.model;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipInputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

class EpubStorageHelper {

    static File getOpfPath(Epub epub) {
        String relativeOpfPath = "";
        try {
            // get the OPF path, directly from container.xml

            BufferedReader br
                    = new BufferedReader(new InputStreamReader(new FileInputStream(epub.getLocation()
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
                return epub.getLocation();
            }

            // remove the OPF file name and the preceding '/'
            int last = relativeOpfPath.lastIndexOf('/');
            if (last > -1) {
                relativeOpfPath = relativeOpfPath.substring(0, last);
            }

            return new File(epub.getLocation(), relativeOpfPath);
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
        return epub.getLocation();
    }

    static Epub fromUri(Context context, String uri) throws IOException {
        File cacheDir = new File(context.getCacheDir(), "epubreader_cache");
        cacheDir.mkdirs();
        File unzippedEpubLocation = Unzipper.unzipEpubIfNeeded(context, uri, cacheDir);

        ZipInputStream in = new ZipInputStream(openFromUri(context, uri));
        try {
            Book book = new EpubReader().readEpub(in);
            return new Epub(book, unzippedEpubLocation);
        } finally {
            in.close();
        }
    }

    static InputStream openFromUri(Context context, String uriString) throws IOException {

        Uri uri = Uri.parse(uriString);
        InputStream inputStream;
        if (uriString.startsWith("file:///android_asset/")) {
            inputStream = context.getAssets().open(uriString.replace("file:///android_asset/", ""));
        } else {
            inputStream = context.getContentResolver().openInputStream(uri);
        }
        return inputStream;
    }
}
