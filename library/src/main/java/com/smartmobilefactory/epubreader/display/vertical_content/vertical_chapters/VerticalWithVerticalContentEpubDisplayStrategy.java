package com.smartmobilefactory.epubreader.display.vertical_content.vertical_chapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.EpubView;
import com.smartmobilefactory.epubreader.UrlInterceptor;
import com.smartmobilefactory.epubreader.display.EpubDisplayStrategy;
import com.smartmobilefactory.epubreader.display.binding.EpubVerticalVerticalContentBinding;
import com.smartmobilefactory.epubreader.display.view.EpubWebView;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class VerticalWithVerticalContentEpubDisplayStrategy extends EpubDisplayStrategy {

    private EpubVerticalVerticalContentBinding binding;

    private EpubView epubView;

    private ChapterAdapter chapterAdapter;

    private PublishSubject<Pair<Integer, Integer>> scrollPosition = PublishSubject.create();

    final UrlInterceptor urlInterceptor = url -> epubView.shouldOverrideUrlLoading(url);

    @Override
    public void bind(EpubView epubView, ViewGroup parent) {
        this.epubView = epubView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = EpubVerticalVerticalContentBinding.inflate(inflater, parent, true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(epubView.getContext());
        layoutManager.setInitialPrefetchItemCount(2);
        binding.recyclerview.setLayoutManager(layoutManager);
        chapterAdapter = new ChapterAdapter(this, epubView);
        binding.recyclerview.setAdapter(chapterAdapter);
        binding.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                View firstVisibleView = layoutManager.findViewByPosition(firstVisibleItemPosition);
                scrollPosition.onNext(new Pair<>(firstVisibleItemPosition, firstVisibleView.getTop()));
                setCurrentChapter(firstVisibleItemPosition);
            }
        });

        scrollPosition
                .sample(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(positionTopPair -> {
                    ChapterAdapter.ChapterViewHolder holder = (ChapterAdapter.ChapterViewHolder) binding.recyclerview.findViewHolderForAdapterPosition(positionTopPair.first);
                    if (holder != null) {
                        float density = epubView.getContext().getResources().getDisplayMetrics().density;
                        holder.binding.webview.callJavascriptMethod("updateFirstVisibleElementByTopPosition", -positionTopPair.second/density);
                    }
                })
                .subscribe(new BaseDisposableObserver<>());
    }

    @Override
    public void displayEpub(final Epub epub, EpubLocation location) {
        chapterAdapter.displayEpub(epub, location);

        if (location instanceof EpubLocation.ChapterLocation) {
            binding.recyclerview.scrollToPosition(((EpubLocation.ChapterLocation) location).chapter());
        }
    }

    @Override
    public void gotoLocation(EpubLocation location) {
        if (location instanceof EpubLocation.ChapterLocation) {
            EpubLocation.ChapterLocation chapterLocation = (EpubLocation.ChapterLocation) location;

            this.binding.recyclerview.scrollToPosition(chapterLocation.chapter());
            displayEpub(epubView.getEpub(), location);
            setCurrentLocation(location);
        }
    }

    @Override
    public void callChapterJavascriptMethod(int chapter, String name, Object... args) {
        if (chapterAdapter == null) {
            return;
        }
        ChapterAdapter.ChapterViewHolder holder = (ChapterAdapter.ChapterViewHolder) binding.recyclerview.findViewHolderForAdapterPosition(chapter);
        if (holder != null) {
            holder.binding.webview.callJavascriptMethod(name, args);
        }
    }

    @Override
    public void callChapterJavascriptMethod(String name, Object... args) {
        callChapterJavascriptMethod(getCurrentChapter(), name, args);
    }

    void scrollTo(EpubLocation location, int chapter, int offsetY) {
        binding.recyclerview.post(() -> {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerview.getLayoutManager();
            linearLayoutManager.scrollToPositionWithOffset(chapter, -offsetY);
            setCurrentLocation(location);
        });
    }

}
