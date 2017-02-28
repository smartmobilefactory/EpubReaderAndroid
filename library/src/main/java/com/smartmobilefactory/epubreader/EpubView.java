package com.smartmobilefactory.epubreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.List;

import com.smartmobilefactory.epubreader.display.EpubDisplayStrategy;
import com.smartmobilefactory.epubreader.display.vertical_content.horizontal_chapters.HorizontalWithVerticalContentEpubDisplayStrategy;
import com.smartmobilefactory.epubreader.display.vertical_content.SingleChapterVerticalEpubDisplayStrategy;
import com.smartmobilefactory.epubreader.display.view.EpubWebView;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import io.reactivex.Observable;
import nl.siegmann.epublib.domain.SpineReference;

public class EpubView extends FrameLayout {

    private Epub epub;
    private final EpubViewSettings epubViewSettings = new EpubViewSettings();
    private EpubDisplayStrategy strategy;
    private EpubScrollDirection scrollDirection;

    private EpubWebView.UrlInterceptor urlInterceptor;

    public EpubView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public EpubView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EpubView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EpubView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attributeSet) {
        setScrollDirection(EpubScrollDirection.HORIZONTAL_WITH_VERTICAL_CONTENT);
    }

    public void setScrollDirection(EpubScrollDirection scrollDirection) {
        this.scrollDirection = scrollDirection;
        switch (scrollDirection) {
            case HORIZONTAL_WITH_VERTICAL_CONTENT:
                applyDisplayStrategy(new HorizontalWithVerticalContentEpubDisplayStrategy());
                break;
            case SINGLE_CHAPTER_VERTICAL:
                applyDisplayStrategy(new SingleChapterVerticalEpubDisplayStrategy());
                break;
        }
    }

    public EpubScrollDirection getScrollDirection() {
        return scrollDirection;
    }

    public EpubViewSettings getSettings() {
        return epubViewSettings;
    }

    private void applyDisplayStrategy(@NonNull EpubDisplayStrategy newStrategy) {
        if (strategy != null) {
            // unbind and remove current strategy
            strategy.unbind();
            strategy = null;
            removeAllViews();
        }
        strategy = newStrategy;
        strategy.bind(this, this);
        if (epub != null) {
            setEpub(epub);
        }
    }

    public void setEpub(final Epub epub) {
        setEpub(epub, null);
    }

    public void setEpub(final Epub epub, EpubLocation location) {
        if (epub == null) {
            throw new IllegalArgumentException("epub must not be null");
        }

        if (location == null) {
            location = EpubLocation.fromChapter(0);
        }

        if (this.epub == epub) {
            gotoLocation(location);
            return;
        }

        this.epub = epub;
        strategy.displayEpub(epub, location);
    }

    public Epub getEpub() {
        return epub;
    }

    public void setUrlInterceptor(EpubWebView.UrlInterceptor interceptor) {
        this.urlInterceptor = interceptor;
    }

    public boolean shouldOverrideUrlLoading(String url) {

        if (url.startsWith(Uri.fromFile(epub.getLocation()).toString())) {
            // internal chapter change url
            List<SpineReference> spineReferences = epub.getBook().getSpine().getSpineReferences();
            for (int i = 0; i < spineReferences.size(); i++) {
                SpineReference spineReference = spineReferences.get(i);
                if (url.endsWith(spineReference.getResource().getHref())) {
                    gotoLocation(EpubLocation.fromChapter(i));
                    return true;
                }
            }
            // chapter not found
            // can not open the url
            return true;
        }

        if (urlInterceptor != null && urlInterceptor.shouldOverrideUrlLoading(url)) {
            return true;
        }

        try {
            // try to open url with external app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            getContext().startActivity(intent);
        } catch (Exception e) {
            // ignore
        }

        // we never want to load a new url in the same webview
        return true;
    }

    public void gotoLocation(EpubLocation location) {
        if (epub == null) {
            throw new IllegalStateException("setEpub must be called first");
        }
        strategy.gotoLocation(location);
    }

    public int getCurrentChapter() {
        return strategy.getCurrentChapter();
    }

    public Observable<Integer> chapter() {
        return strategy.onChapterChanged().distinctUntilChanged();
    }

    public EpubLocation.XPathLocation getCurrentLocation() {
        return strategy.getCurrentLocation();
    }

    public Observable<EpubLocation.XPathLocation> currentLocation() {
        return strategy.currentLocation().distinctUntilChanged();
    }

    /**
     * calls a javascript method on all visible chapters
     * this depends on the selected display strategy
     */
    public void callChapterJavascriptMethod(String name, Object... args) {
        strategy.callChapterJavascriptMethod(name, args);
    }

    /**
     * calls a javascript method on the selected chapter if visible
     * this depends on the selected display strategy
     */
    public void callChapterJavascriptMethod(int chapter, String name, Object... args) {
        strategy.callChapterJavascriptMethod(chapter, name, args);
    }

}
