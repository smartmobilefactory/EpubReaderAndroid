package com.smartmobilefactory.epubreader.model;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EpubFont {

    @Nullable
    public abstract String name();

    @Nullable
    public abstract String uri();

    public static EpubFont fromUri(String name, String uri) {
        return new AutoValue_EpubFont(name, uri);
    }

    public static EpubFont fromFontFamiliy(String fontFamiliy) {
        return new AutoValue_EpubFont(fontFamiliy, null);
    }

}
