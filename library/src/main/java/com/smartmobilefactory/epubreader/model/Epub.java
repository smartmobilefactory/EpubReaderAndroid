package com.smartmobilefactory.epubreader.model;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipInputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class Epub {

    private static final String TAG = Epub.class.getSimpleName();

    private File opfPath = null;
    private final File location;
    private final Book book;

    Epub(Book book, File location) {
        this.location = location;
        this.book = book;
    }

    public Book getBook() {
        return book;
    }

    public File getOpfPath() {
        if (opfPath != null) {
            return opfPath;
        }
        String relativeOpfPath = "";
        try {
            // get the OPF path, directly from container.xml

            BufferedReader br
                    = new BufferedReader(new InputStreamReader(new FileInputStream(getLocation()
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
                opfPath = getLocation();
                return opfPath;
            }

            // remove the OPF file name and the preceding '/'
            int last = relativeOpfPath.lastIndexOf('/');
            if (last > -1) {
                relativeOpfPath = relativeOpfPath.substring(0, last);
            }

            return opfPath = new File(getLocation(), relativeOpfPath);
        } catch (NullPointerException | IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return opfPath;
    }

    public File getLocation() {
        return location;
    }


    /**
     * @param context
     * @param uri locale file uri. Asset uri is allowed
     * @throws IOException
     */
    @WorkerThread
    public static Epub fromUri(Context context, String uri) throws IOException {
        File cacheDir = new File(context.getCacheDir(), "epubreader_cache");
        cacheDir.mkdirs();
        Pair<File, File> epub = Unzipper.unzipEpubIfNeeded(context, uri, cacheDir);

        ZipInputStream in = new ZipInputStream(new FileInputStream(epub.second));
        try {
            Book book = new EpubReader().readEpub(in);
            return new Epub(book, epub.first);
        } finally {
            in.close();
        }
    }

}
