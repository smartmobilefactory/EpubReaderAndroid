package com.smartmobilefactory.epubreader.display.vertical_content.horizontal_chapters;

import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.EpubView;
import com.smartmobilefactory.epubreader.databinding.EpubHorizontalVerticalContentBinding;
import com.smartmobilefactory.epubreader.databinding.ItemEpubVerticalContentBinding;
import com.smartmobilefactory.epubreader.display.EpubDisplayStrategy;
import com.smartmobilefactory.epubreader.display.view.EpubWebView;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubLocation;

public class HorizontalWithVerticalContentEpubDisplayStrategy extends EpubDisplayStrategy {

    private EpubHorizontalVerticalContentBinding binding;

    private EpubView epubView;
    private PagerAdapter pagerAdapter;

    final EpubWebView.UrlInterceptor urlInterceptor = url -> epubView.shouldOverrideUrlLoading(url);

    @Override
    public void bind(EpubView epubView, ViewGroup parent) {
        this.epubView = epubView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = EpubHorizontalVerticalContentBinding.inflate(inflater, parent, true);

        binding.pager.setOffscreenPageLimit(1);
        binding.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setCurrentChapter(position);
                EpubLocation location = pagerAdapter.getChapterLocation(position);
                if (location == null) {
                    location = EpubLocation.fromChapter(position);
                }
                setCurrentLocation(location);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        pagerAdapter = new PagerAdapter(this, epubView);
        binding.pager.setAdapter(pagerAdapter);
    }

    @Override
    public void displayEpub(final Epub epub, EpubLocation location) {
        pagerAdapter.displayEpub(epub, location);

        if (location instanceof EpubLocation.ChapterLocation) {
            binding.pager.setCurrentItem(((EpubLocation.ChapterLocation) location).chapter());
        }

    }

    @Override
    public void gotoLocation(EpubLocation location) {
        if (location instanceof EpubLocation.ChapterLocation) {
            EpubLocation.ChapterLocation chapterLocation = (EpubLocation.ChapterLocation) location;

            ItemEpubVerticalContentBinding binding = pagerAdapter.getViewBindingIfAttached(chapterLocation.chapter());
            if (binding != null) {
                binding.webview.gotoLocation(location);
            } else {
                displayEpub(epubView.getEpub(), location);
            }

            this.binding.pager.setCurrentItem(chapterLocation.chapter());
            setCurrentLocation(location);
        }
    }

    @Override
    public void callChapterJavascriptMethod(int chapter, String name, Object... args) {
        if (pagerAdapter == null) {
            return;
        }
        ItemEpubVerticalContentBinding binding = pagerAdapter.getViewBindingIfAttached(chapter);
        if (binding != null) {
            binding.webview.callJavascriptMethod(name, args);
        }
    }

    @Override
    public void callChapterJavascriptMethod(String name, Object... args) {
        for (ItemEpubVerticalContentBinding binding : pagerAdapter.getAttachedViewBindings()) {
            binding.webview.callJavascriptMethod(name, args);
        }
    }

    @Override
    protected void setCurrentLocation(EpubLocation location) {
        // overridden to increase visibility to package
        super.setCurrentLocation(location);
    }

}
