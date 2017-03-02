package com.smartmobilefactory.epubreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.smartmobilefactory.epubreader.display.EpubDisplayStrategy;
import com.smartmobilefactory.epubreader.display.vertical_content.SingleChapterVerticalEpubDisplayStrategy;
import com.smartmobilefactory.epubreader.display.vertical_content.horizontal_chapters.HorizontalWithVerticalContentEpubDisplayStrategy;
import com.smartmobilefactory.epubreader.display.view.EpubWebView;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import nl.siegmann.epublib.domain.SpineReference;

public class EpubView extends FrameLayout {

    private Epub epub;
    private final EpubViewSettings epubViewSettings = new EpubViewSettings();

    private BehaviorSubject<Integer> currentChapterSubject = BehaviorSubject.create();
    private BehaviorSubject<EpubLocation> currentLocationSubject = BehaviorSubject.create();

    private final CompositeDisposable strategyDisposables = new CompositeDisposable();
    private EpubDisplayStrategy strategy;
    private EpubScrollDirection scrollDirection;

    private SavedState savedState;

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
            strategyDisposables.clear();
            strategy.unbind();
            strategy = null;
            removeAllViews();
        }
        strategy = newStrategy;
        strategy.bind(this, this);

        strategy.currentLocation()
                .doOnNext(location -> currentLocationSubject.onNext(location))
                .subscribeWith(new BaseDisposableObserver<>())
                .addTo(strategyDisposables);

        strategy.onChapterChanged()
                .doOnNext(chapter -> currentChapterSubject.onNext(chapter))
                .subscribeWith(new BaseDisposableObserver<>())
                .addTo(strategyDisposables);

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
            if (savedState != null && Uri.fromFile(epub.getLocation()).toString().equals(savedState.epubUri)) {
                location = savedState.location;
            } else {
                location = EpubLocation.fromChapter(0);
            }
            savedState = null;
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
        return currentChapterSubject.getValue();
    }

    public Observable<Integer> currentChapter() {
        return currentChapterSubject.distinctUntilChanged();
    }

    public EpubLocation getCurrentLocation() {
        return currentLocationSubject.getValue();
    }

    public Observable<EpubLocation> currentLocation() {
        return currentLocationSubject.distinctUntilChanged();
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

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        if (epub != null) {
            ss.epubUri = Uri.fromFile(epub.getLocation()).toString();
        }

        ss.location = strategy.getCurrentLocation();
        if (savedState != null) {
            ss.location = savedState.location;
        }

        if (ss.location == null) {
            ss.location = EpubLocation.fromChapter(strategy.getCurrentChapter());
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (epub != null && Uri.fromFile(epub.getLocation()).toString().equals(ss.epubUri)) {
            gotoLocation(ss.location);
        } else {
            savedState = ss;
        }

    }

    private static class SavedState extends AbsSavedState {

        String epubUri;
        EpubLocation location;
        ClassLoader loader;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(epubUri);
            out.writeParcelable(location, flags);
        }

        @Override
        public String toString() {
            return "EpubView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " location=" + location + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(
                new ParcelableCompatCreatorCallbacks<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        return new SavedState(in, loader);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                });

        SavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            if (loader == null) {
                loader = getClass().getClassLoader();
            }
            epubUri = in.readString();
            location = in.readParcelable(loader);
            this.loader = loader;
        }
    }

}
