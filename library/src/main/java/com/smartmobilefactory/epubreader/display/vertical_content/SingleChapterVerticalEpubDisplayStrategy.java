package com.smartmobilefactory.epubreader.display.vertical_content;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.EpubView;
import com.smartmobilefactory.epubreader.EpubViewSettings;
import com.smartmobilefactory.epubreader.databinding.ItemEpubVerticalContentBinding;
import com.smartmobilefactory.epubreader.display.EpubDisplayHelper;
import com.smartmobilefactory.epubreader.display.EpubDisplayStrategy;
import com.smartmobilefactory.epubreader.display.view.InternalEpubBridge;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import nl.siegmann.epublib.domain.SpineReference;

public class SingleChapterVerticalEpubDisplayStrategy extends EpubDisplayStrategy {

    private static final String TAG = SingleChapterVerticalEpubDisplayStrategy.class.getSimpleName();
    private EpubView epubView;
    private ItemEpubVerticalContentBinding binding;

    private EpubViewSettings settings;

    @Override
    public void bind(EpubView epubView, ViewGroup parent) {
        this.epubView = epubView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemEpubVerticalContentBinding.inflate(inflater, parent, true);
        settings = epubView.getSettings();
    }

    @Override
    public void displayEpub(final Epub epub, EpubLocation location) {
        if (location instanceof EpubLocation.ChapterLocation) {
            int chapter = ((EpubLocation.ChapterLocation) location).chapter();
            SpineReference spineReference = epub.getBook().getSpine().getSpineReferences().get(chapter);
            EpubDisplayHelper.loadHtmlData(binding.webview, epub, spineReference, settings);
            setCurrentChapter(chapter);
        }

        binding.webview.gotoLocation(location);
        binding.webview.setUrlInterceptor(url -> epubView.shouldOverrideUrlLoading(url));
        binding.webview.bindToSettings(settings);

        InternalEpubBridge bridge = new InternalEpubBridge();
        binding.webview.setInternalBridge(bridge);

        bridge.xPath().subscribe(xPath -> {
            setCurrentLocation(EpubLocation.fromXPath(getCurrentChapter(), xPath));
        });

        VerticalContentBinderHelper.bind(binding);
    }

    @Override
    public void gotoLocation(EpubLocation location) {
        if (location instanceof EpubLocation.ChapterLocation) {
            EpubLocation.ChapterLocation chapterLocation = (EpubLocation.ChapterLocation) location;
            if (getCurrentChapter() == chapterLocation.chapter()) {
                binding.webview.gotoLocation(location);
            } else {
                displayEpub(epubView.getEpub(), location);
            }
            setCurrentLocation(location);
        }
    }

    @Override
    public void callChapterJavascriptMethod(int chapter, String name, Object... args) {
        if (chapter == getCurrentChapter()) {
            callChapterJavascriptMethod(name, args);
        }
    }

    @Override
    public void callChapterJavascriptMethod(String name, Object... args) {
        binding.webview.callJavascriptMethod(name, args);
    }
}
