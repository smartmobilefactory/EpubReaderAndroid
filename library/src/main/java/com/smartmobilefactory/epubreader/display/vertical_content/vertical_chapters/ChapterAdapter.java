package com.smartmobilefactory.epubreader.display.vertical_content.vertical_chapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.EpubView;
import com.smartmobilefactory.epubreader.databinding.ItemEpubVerticalContentBinding;
import com.smartmobilefactory.epubreader.databinding.ItemVerticalVerticalContentBinding;
import com.smartmobilefactory.epubreader.display.EpubDisplayHelper;
import com.smartmobilefactory.epubreader.display.vertical_content.VerticalContentBinderHelper;
import com.smartmobilefactory.epubreader.display.view.InternalEpubBridge;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver;

import io.reactivex.disposables.CompositeDisposable;
import nl.siegmann.epublib.domain.SpineReference;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private EpubView epubView;
    private VerticalWithVerticalContentEpubDisplayStrategy strategy;

    private Epub epub;
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

        holder.binding.webview.loadUrl("about:blank");
        holder.binding.webview.setUrlInterceptor(strategy.urlInterceptor);

        SpineReference spineReference = epub.getBook().getSpine().getSpineReferences().get(position);
        EpubDisplayHelper.loadHtmlData(holder.binding.webview, epub, spineReference, epubView.getSettings())
                .addTo(compositeDisposable);

        InternalEpubBridge bridge = new InternalEpubBridge();
        holder.binding.webview.setInternalBridge(bridge);

        bridge.xPath()
                .doOnNext(xPath -> {
                    EpubLocation location = EpubLocation.fromXPath(strategy.getCurrentChapter(), xPath);
                    strategy.setCurrentLocation(location);
                })
                .subscribeWith(new BaseDisposableObserver<>())
                .addTo(compositeDisposable);
    }

    @Override
    public int getItemCount() {
        if (epub == null) {
            return 0;
        }
        return epub.getBook().getSpine().getSpineReferences().size();
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
