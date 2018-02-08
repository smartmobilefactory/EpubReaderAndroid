package com.smartmobilefactory.epubreader.sample;

import android.graphics.Color;

import com.smartmobilefactory.epubreader.EpubView;
import com.smartmobilefactory.epubreader.EpubViewPlugin;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NightmodePlugin extends EpubViewPlugin {

    private EpubView epubView;

    public NightmodePlugin(EpubView epubView) {
        this.epubView = epubView;
    }

    private boolean nightModeEnabled = false;

    public void setNightModeEnabled(boolean enabled) {
        nightModeEnabled = enabled;
        if (nightModeEnabled) {
            epubView.setBackgroundColor(Color.parseColor("#111111"));
        } else {
            epubView.setBackgroundColor(Color.WHITE);
        }
        notifyDataChanged();
    }

    @NotNull
    @Override
    public List<String> getCustomChapterCss() {
        if (nightModeEnabled) {
            return Collections.singletonList("file:///android_asset/books/styles/night_mode.css");
        } else {
            return Collections.emptyList();
        }
    }
}