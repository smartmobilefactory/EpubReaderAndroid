package com.smartmobilefactory.epubreader.display.vertical_content.vertical_chapters;

import android.support.annotation.Keep;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;

import com.smartmobilefactory.epubreader.EpubView;
import com.smartmobilefactory.epubreader.databinding.ItemVerticalVerticalContentBinding;
import com.smartmobilefactory.epubreader.display.EpubDisplayHelper;
import com.smartmobilefactory.epubreader.display.view.EpubWebView;
import com.smartmobilefactory.epubreader.display.view.InternalEpubBridge;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import nl.siegmann.epublib.domain.SpineReference;

class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private static final String BLANK_URL = "about:blank";

    private EpubView epubView;
    private VerticalWithVerticalContentEpubDisplayStrategy strategy;

    private Epub epub;

    private int locationChapter;
    private EpubLocation tempLocation;
    private EpubLocation location;

    ChapterAdapter(VerticalWithVerticalContentEpubDisplayStrategy strategy, EpubView epubView) {
        this.strategy = strategy;
        this.epubView = epubView;
    }

    void displayEpub(Epub epub, EpubLocation location) {
        this.epub = epub;
        this.location = location;
        notifyDataSetChanged();
    }

    @Override
    public ChapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ChapterViewHolder holder = ChapterViewHolder.create(parent);
        holder.binding.webview.bindToSettings(epubView.getSettings());
        return holder;
    }

    @Override
    public void onBindViewHolder(ChapterViewHolder holder, int position) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();

        holder.binding.webview.loadUrl(BLANK_URL);
        holder.binding.webview.setUrlInterceptor(strategy.urlInterceptor);

        SpineReference spineReference = epub.getBook().getSpine().getSpineReferences().get(position);
        EpubDisplayHelper.loadHtmlData(holder.binding.webview, epub, spineReference, epubView.getSettings())
                .addTo(compositeDisposable);

        Bridge bridge = new Bridge();
        holder.binding.webview.setInternalBridge(bridge);

        handleLocation(position, holder.binding.webview);

        bridge.xPath()
                .doOnNext(xPath -> {
                    EpubLocation location = EpubLocation.fromXPath(strategy.getCurrentChapter(), xPath);
                    strategy.setCurrentLocation(location);
                })
                .subscribeWith(new BaseDisposableObserver<>())
                .addTo(compositeDisposable);
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
            locationChapter = chapter;
            tempLocation = location;
            location = null;
            webView.isReady()
                    .filter(isReady -> isReady && !webView.getUrl().equals(BLANK_URL))
                    .take(1)
                    .delay(1000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(isReady -> {
                        if (tempLocation instanceof EpubLocation.IdLocation) {
                            webView.callJavascriptMethod("getYPositionOfElementWithId", ((EpubLocation.IdLocation) tempLocation).id());
                        } else if (tempLocation instanceof EpubLocation.XPathLocation) {
                            webView.callJavascriptMethod("getYPositionOfElementWithXPath", ((EpubLocation.XPathLocation) tempLocation).xPath());
                        } else if (tempLocation instanceof EpubLocation.RangeLocation){
                            webView.callJavascriptMethod("getYPositionOfElementFromRangeStart", ((EpubLocation.RangeLocation) tempLocation).start());
                        }
                    })
                    .subscribe(new BaseDisposableObserver<>());
        }

    }

    @Override
    public int getItemCount() {
        if (epub == null) {
            return 0;
        }
        return epub.getBook().getSpine().getSpineReferences().size();
    }

    private class Bridge extends InternalEpubBridge {

        @Keep
        @JavascriptInterface
        public void resultGetYPositionOfElement(int top) {
            float density = epubView.getContext().getResources().getDisplayMetrics().density;
            strategy.scrollTo(tempLocation, locationChapter, (int) (top * density));
        }

    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {

        ItemVerticalVerticalContentBinding binding;

        static ChapterViewHolder create(ViewGroup parent) {
            return new ChapterViewHolder(ItemVerticalVerticalContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        ChapterViewHolder(ItemVerticalVerticalContentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
