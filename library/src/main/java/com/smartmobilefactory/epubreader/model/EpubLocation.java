package com.smartmobilefactory.epubreader.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

public abstract class EpubLocation implements Parcelable {

    EpubLocation() {
    }

    public static EpubLocation fromID(@NonNull int chapter, @NonNull String id) {
        return new AutoValue_EpubLocation_IdLocation(chapter, id);
    }

    public static EpubLocation fromRange(@NonNull int chapter, int start, int end) {
        return new AutoValue_EpubLocation_RangeLocation(chapter, start, end);
    }

    public static ChapterLocation fromChapter(@NonNull int chapter) {
        return new AutoValue_EpubLocation_ChapterLocationImpl(chapter);
    }

    public static XPathLocation fromXPath(int chapter, String xPath) {
        return new AutoValue_EpubLocation_XPathLocation(chapter, xPath);
    }

    public abstract static class ChapterLocation extends EpubLocation {
        public abstract int chapter();
    }

    @AutoValue
    public abstract static class ChapterLocationImpl extends ChapterLocation {
    }

    @AutoValue
    public abstract static class IdLocation extends ChapterLocation {
        public abstract String id();
    }

    @AutoValue
    public abstract static class XPathLocation extends ChapterLocation {
        public abstract String xPath();
    }

    @AutoValue
    public abstract static class RangeLocation extends ChapterLocation {
        public abstract int start();

        public abstract int end();
    }

}
