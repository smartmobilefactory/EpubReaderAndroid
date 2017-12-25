package com.smartmobilefactory.epubreader.model;

import java.io.File;
import java.io.IOException;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.epub.EpubReader;

class UncompressedEpubReader {

    static Book readUncompressedBook(File folder) throws IOException {
        Resources resources = readLazyResources(folder);
        return new EpubReader().readEpub(resources);
    }

    private static Resources readLazyResources(File folder) throws IOException {
        Resources result = new Resources();
        readLazyResources(folder, result, folder);
        return result;
    }

    private static void readLazyResources(File root, Resources resources, File folder) throws IOException {
        String hrefRoot = root.getAbsolutePath() + "/";
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                readLazyResources(root, resources, file);
                continue;
            }
            if (file.getName().equals(".ready")) {
                continue;
            }

            String path = file.getAbsolutePath();
            String href = path.replace(hrefRoot, "");
            Resource resource = new LazyResource(path, 0, href);
            resources.add(resource);
        }
    }

}
