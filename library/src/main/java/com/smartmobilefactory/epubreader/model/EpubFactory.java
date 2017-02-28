package com.smartmobilefactory.epubreader.model;

import android.content.Context;
import android.support.v4.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class EpubFactory {

    /**
     * @param context
     * @param uri locale file uri. Asset uri is allowed
     * @throws IOException
     */
    public static Epub fromUri(Context context, String uri) throws IOException {
        File cacheDir = new File(context.getCacheDir(), "epubreader_cache");
        cacheDir.mkdirs();
        Pair<File, File> epub = Unzipper.unzipEpubIfNeeded(context, uri, cacheDir);
        Book book = new EpubReader().readEpub(new FileInputStream(epub.second));
        return new Epub(book, epub.first);
    }
}
