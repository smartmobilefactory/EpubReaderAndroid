package com.smartmobilefactory.epubreader.display;

import android.support.annotation.CallSuper;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.EpubView;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

public abstract class EpubDisplayStrategy {

    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final BehaviorSubject<Integer> currentChapter = BehaviorSubject.createDefault(0);

    private final BehaviorSubject<EpubLocation> currentLocation = BehaviorSubject.create();

    public abstract void bind(EpubView epubView, ViewGroup parent);

    @CallSuper
    public void unbind() {
        compositeDisposable.clear();
    }

    public abstract void displayEpub(Epub epub, EpubLocation location);

    public abstract void gotoLocation(EpubLocation location);

    protected void setCurrentChapter(int position) {
        currentChapter.onNext(position);
    }

    public int getCurrentChapter() {
        return currentChapter.getValue();
    }

    public Observable<Integer> onChapterChanged() {
        return currentChapter;
    }

    protected void setCurrentLocation(EpubLocation location) {
        currentLocation.onNext(location);
    }

    public EpubLocation getCurrentLocation() {
        return currentLocation.getValue();
    }

    public Observable<EpubLocation> currentLocation() {
        return currentLocation;
    }

    /**
     * calls a javascript method on all visible chapters
     * this depends on the selected display strategy
     */
    public void callChapterJavascriptMethod(String name, Object... args) {
    }

    /**
     * calls a javascript method on the selected chapter if visible
     * this depends on the selected display strategy
     */
    public void callChapterJavascriptMethod(int chapter, String name, Object... args) {
    }

}
