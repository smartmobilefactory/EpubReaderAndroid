package com.smartmobilefactory.epubreader.display.vertical_content.horizontal_chapters;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.EpubView;
import com.smartmobilefactory.epubreader.display.EpubDisplayHelper;
import com.smartmobilefactory.epubreader.display.vertical_content.VerticalContentBinderHelper;
import com.smartmobilefactory.epubreader.display.view.BaseViewPagerAdapter;
import com.smartmobilefactory.epubreader.display.view.EpubWebView;
import com.smartmobilefactory.epubreader.display.view.InternalEpubBridge;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver;

import com.smartmobilefactory.epubreader.databinding.ItemEpubVerticalContentBinding;
import io.reactivex.disposables.CompositeDisposable;
import nl.siegmann.epublib.domain.SpineReference;

class PagerAdapter extends BaseViewPagerAdapter {

    private final SparseArray<CompositeDisposable> chapterDisposables = new SparseArray<>();

    private EpubView epubView;
    private HorizontalWithVerticalContentEpubDisplayStrategy strategy;

    private Epub epub;
    private EpubLocation location;

    PagerAdapter(HorizontalWithVerticalContentEpubDisplayStrategy strategy, EpubView epubView) {
        this.strategy = strategy;
        this.epubView = epubView;
    }

    void displayEpub(Epub epub, EpubLocation location) {
        this.epub = epub;
        this.location = location;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, ViewGroup parent) {
        CompositeDisposable compositeDisposable = chapterDisposables.get(position);
        if (compositeDisposable == null) {
            chapterDisposables.append(position, new CompositeDisposable());
            compositeDisposable = chapterDisposables.get(position);
        }

        ItemEpubVerticalContentBinding binding = ItemEpubVerticalContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        binding.webview.setUrlInterceptor(strategy.urlInterceptor);

        SpineReference spineReference = epub.getBook().getSpine().getSpineReferences().get(position);
        EpubDisplayHelper.loadHtmlData(binding.webview, epub, spineReference, epubView.getSettings());

        handleLocation(position, binding.webview);

        binding.webview.bindToSettings(epubView.getSettings());

        InternalEpubBridge bridge = new InternalEpubBridge();
        binding.webview.setInternalBridge(bridge);

        bridge.xPath()
                .doOnNext(xPath -> {
                    strategy.setCurrentLocation(EpubLocation.fromXPath(strategy.getCurrentChapter(), xPath));
                })
                .subscribeWith(new BaseDisposableObserver<>())
                .addTo(compositeDisposable);

        compositeDisposable.add(VerticalContentBinderHelper.bind(binding));

        return binding.getRoot();
    }

    private void handleLocation(int position, EpubWebView webView) {

        if (location == null) {
            return;
        }

        final int chapter;
        if (location instanceof EpubLocation.ChapterLocation) {
            chapter = ((EpubLocation.ChapterLocation) location).chapter();
        } else {
            chapter = 0;
        }

        if (position == chapter) {
            webView.gotoLocation(location);
            location = null;
        }

    }

    @Override
    public void onItemDestroyed(int position, View view) {
        super.onItemDestroyed(position, view);
        CompositeDisposable compositeDisposable = chapterDisposables.get(position);
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    @Override
    public int getCount() {
        if (epub == null) {
            return 0;
        }
        return epub.getBook().getSpine().getSpineReferences().size();
    }
}
