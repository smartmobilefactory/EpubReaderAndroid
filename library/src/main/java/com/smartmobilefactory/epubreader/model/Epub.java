package com.smartmobilefactory.epubreader.model;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;

import com.smartmobilefactory.epubreader.EpubView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipInputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

public class Epub {

    private File opfPath = null;
    private final File location;
    private final Book book;

    /**
     * create a new instance by calling {@link #fromUri(Context, String)}
     */
    Epub(Book book, File location) {
        this.location = location;
        this.book = book;
    }

    public Book getBook() {
        return book;
    }

    /**
     * @return -1 if toc position not found
     */
    public int getSpinePositionForTocReference(TOCReference tocReference) {
        List<SpineReference> spineReferences = getBook().getSpine().getSpineReferences();
        for (int i = 0; i < spineReferences.size(); i++) {
            SpineReference spineReference = spineReferences.get(i);
            if (tocReference.getResourceId().equals(spineReference.getResourceId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return -1 if spine position not found
     */
    public int getTocPositionForSpineReference(SpineReference spineReference) {
        List<SpineReference> spineReferences = getBook().getSpine().getSpineReferences();
        for (int i = 0; i < spineReferences.size(); i++) {
            SpineReference spineReference2 = spineReferences.get(i);
            if (spineReference2.getResourceId().equals(spineReference.getResourceId())) {
                return getTocPositionForSpinePosition(i);
            }
        }
        return -1;
    }

    /**
     * @return -1 if spine position not found
     */
    public int getTocPositionForSpinePosition(int spinePosition) {
        List<TOCReference> tocReferences = getBook().getTableOfContents().getTocReferences();
        for (int i = 0; i < tocReferences.size(); i++) {
            TOCReference tocReference = tocReferences.get(i);
            int spinePositionForTocReference = getSpinePositionForTocReference(tocReference);
            if (spinePositionForTocReference == spinePosition) {
                return i;
            }
            if (spinePositionForTocReference > spinePosition) {
                return i - 1;
            }
        }
        return -1;
    }

    public File getOpfPath() {
        if (opfPath != null) {
            return opfPath;
        }
        opfPath = EpubStorageHelper.getOpfPath(this);
        return opfPath;
    }

    public File getLocation() {
        return location;
    }

    /**
     * <h5>Accepts the following URI schemes:</h5>
     * <ul>
     * <li>content</li>
     * <li>android.resource</li>
     * <li>file</li>
     * <li>file/android_assets</li>
     * </ul>
     *
     * @param context
     * @param uri
     * @throws IOException
     */
    @WorkerThread
    public static Epub fromUri(Context context, String uri) throws IOException {
        return EpubStorageHelper.fromUri(context, uri);
    }

    /**
     * removes all cached extracted data for this epub
     * the original epub file will not be removed
     *
     * !!! it is not usable after calling this function !!!
     * make sure the epub is not currently displayed in any {@link EpubView}
     *
     * you need to recreate the epub with {@link #fromUri(Context, String)} again
     */
    @WorkerThread
    public void destroy() throws IOException {
        FileUtils.deleteDirectory(getLocation());
    }


    /**
     * @see #destroy()
     * destroys the cache for an epub without the need of creating an {@link Epub} instance before
     *
     * @param uri epub uri
     */
    @WorkerThread
    public static void destroyForUri(Context context, String uri) throws IOException {
        FileUtils.deleteDirectory(Unzipper.getEpubCacheFolder(EpubStorageHelper.getEpubReaderCacheDir(context), uri));
    }

    /**
     * checks if this epub is destroyed
     */
    public boolean isDestroyed() {
        return !getLocation().exists();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epub)) return false;

        Epub epub = (Epub) o;

        if (opfPath != null ? !opfPath.equals(epub.opfPath) : epub.opfPath != null) {
            return false;
        }
        if (location != null ? !location.equals(epub.location) : epub.location != null) {
            return false;
        }
        return book != null ? book.equals(epub.book) : epub.book == null;

    }

    @Override
    public int hashCode() {
        int result = opfPath != null ? opfPath.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (book != null ? book.hashCode() : 0);
        return result;
    }

}
