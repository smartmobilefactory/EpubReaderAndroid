package com.smartmobilefactory.epubreader.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.zip.ZipInputStream;

import io.reactivex.functions.Consumer;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;

class EpubStorageHelper {

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
        long time = System.currentTimeMillis();
        Log.d("EPUB", "load start");
        File cacheDir = getEpubReaderCacheDir(context);
        File unzippedEpubLocation = Unzipper.unzipEpubIfNeeded(context, uri, cacheDir);
        Log.d("EPUB", "unzip done");
//        ZipInputStream in = new ZipInputStream(openFromUri(context, uri));
        try {
            Book book;
            book = UncompressedEpubReader.readUncompressedBook(unzippedEpubLocation);
//            book = new EpubReader().readEpub(in);
            Log.d("EPUB", "create epub done");
            closeEpubResources(book);
            Log.d("EPUB", "clean up done");

            Log.d("EPUB", "end time: " + (System.currentTimeMillis() - time));

            return new Epub(book, unzippedEpubLocation);
        } finally {
//            in.close();
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

    /**
     * clears unused memory by deleting every cached data from {@link Resource#data}
     * they are mostly only needed during epub processing. needed data should be retrieved using
     * {@link Epub#getResourceContent(Resource)} to keep memory pressure low
     */
    private static void closeEpubResources(Book epubBook) {
        Consumer<Resource> closeResource = new Consumer<Resource>() {

            private Field dataField;

            {
                try {
                    dataField = Resource.class.getDeclaredField("data");
                    dataField.setAccessible(true);
                } catch (Exception e) {
                    // ignore
                }
            }

            @Override
            public void accept(Resource resource) {
                if (resource != null) {
                    resource.close();
                    try {
                        if (dataField != null) {
                            dataField.set(resource, null);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        };

        try {
            closeResource.accept(epubBook.getCoverImage());
            closeResource.accept(epubBook.getCoverPage());
            closeResource.accept(epubBook.getNcxResource());
            closeResource.accept(epubBook.getOpfResource());

            for (Resource resource : epubBook.getResources().getAll()) {
                closeResource.accept(resource);
            }

            for (Resource resource : epubBook.getContents()) {
                closeResource.accept(resource);
            }

            for (SpineReference spineReference : epubBook.getSpine().getSpineReferences()) {
                closeResource.accept(spineReference.getResource());
            }
        } catch (Exception e) {
            // ignore
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
