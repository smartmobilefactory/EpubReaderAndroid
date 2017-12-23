package com.smartmobilefactory.epubreader.model;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.epub.EpubReader;

class UncompressedEpubReader {

    static Book readUncompressedBook(File folder) throws IOException {
        try {
            Book result = new Book();
            Resources resources = readLazyResources(folder);
            Log.d("EPUB", "load resources done");
            resources.remove("mimetype");
            String packageResourceHref = getPackageResourceHref(resources);
            Log.d("EPUB", "getPackageResourceHref done");
            Resource packageResource = processPackageResource(packageResourceHref, result, resources);
            Log.d("EPUB", "processPackageResource done");
            result.setOpfResource(packageResource);
            Resource ncxResource = processNcxResource(packageResource, result);
            Log.d("EPUB", "processNcxResource done");
            result.setNcxResource(ncxResource);
            return result;
        } catch (NoSuchMethodException e) {
            throw new IOException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new IOException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IOException(e.getMessage());
        }
    }

    private static Resource processNcxResource(Resource packageResource, Book book) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method processNcxResource = EpubReader.class.getDeclaredMethod("processNcxResource", Resource.class, Book.class);
        processNcxResource.setAccessible(true);
        return (Resource) processNcxResource.invoke(new EpubReader(), packageResource, book);
    }

    private static Resource processPackageResource(String packageResourceHref, Book book, Resources resources) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method processPackageResource = EpubReader.class.getDeclaredMethod("processPackageResource", String.class, Book.class, Resources.class);
        processPackageResource.setAccessible(true);
        return (Resource) processPackageResource.invoke(new EpubReader(), packageResourceHref, book, resources);
    }

    private static String getPackageResourceHref(Resources resources) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getPackageResourceHref = EpubReader.class.getDeclaredMethod("getPackageResourceHref", Resources.class);
        getPackageResourceHref.setAccessible(true);
        return (String) getPackageResourceHref.invoke(new EpubReader(), resources);
    }

    private static Resources readLazyResources(File folder) throws IOException {
        File opfPath = EpubStorageHelper.getOpfPath(folder);
        Resources result = new Resources();
        readLazyResources(folder, result, folder, opfPath);
        return result;
    }

    private static void readLazyResources(File root, Resources resources, File folder, File opdPath) throws IOException {
        String hrefRoot = root.getAbsolutePath() + "/";
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                readLazyResources(root, resources, file, opdPath);
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
