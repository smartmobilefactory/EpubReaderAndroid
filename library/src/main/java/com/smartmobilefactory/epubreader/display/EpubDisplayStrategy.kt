package com.smartmobilefactory.epubreader.display

import android.support.annotation.CallSuper
import android.view.ViewGroup

import com.smartmobilefactory.epubreader.EpubView
import com.smartmobilefactory.epubreader.model.Epub
import com.smartmobilefactory.epubreader.model.EpubLocation
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

internal abstract class EpubDisplayStrategy {

    protected val compositeDisposable = CompositeDisposable()

    private val currentChapterSubject = BehaviorSubject.createDefault(0)

    private val currentLocationSubject = BehaviorSubject.create<EpubLocation>()

    var currentChapter: Int
        get() = currentChapterSubject.value
        set(position) = currentChapterSubject.onNext(position)

    var currentLocation: EpubLocation
        get() = currentLocationSubject.value
        set(location) = currentLocationSubject.onNext(location)

    abstract fun bind(epubView: EpubView, parent: ViewGroup)

    @CallSuper
    fun unbind() {
        compositeDisposable.clear()
    }

    abstract fun displayEpub(epub: Epub, location: EpubLocation)

    abstract fun gotoLocation(location: EpubLocation)

    fun onChapterChanged(): Observable<Int> {
        return currentChapterSubject
    }

    fun currentLocation(): Observable<EpubLocation> {
        return currentLocationSubject
    }

    /**
     * calls a javascript method on all visible chapters
     * this depends on the selected display strategy
     */
    open fun callChapterJavascriptMethod(name: String, vararg args: Any) {
        callChapterJavascriptMethod(currentChapter, name, args)
    }

    /**
     * calls a javascript method on the selected chapter if visible
     * this depends on the selected display strategy
     */
    abstract fun callChapterJavascriptMethod(chapter: Int, name: String, vararg args: Any)

}
