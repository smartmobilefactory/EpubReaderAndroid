package com.smartmobilefactory.epubreader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import com.smartmobilefactory.epubreader.display.EpubDisplayStrategy
import com.smartmobilefactory.epubreader.display.vertical_content.SingleChapterVerticalEpubDisplayStrategy
import com.smartmobilefactory.epubreader.display.vertical_content.horizontal_chapters.HorizontalWithVerticalContentEpubDisplayStrategy
import com.smartmobilefactory.epubreader.display.vertical_content.vertical_chapters.VerticalWithVerticalContentEpubDisplayStrategy
import com.smartmobilefactory.epubreader.display.view.EpubWebView
import com.smartmobilefactory.epubreader.model.Epub
import com.smartmobilefactory.epubreader.model.EpubLocation
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class EpubView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var epub: Epub? = null
    val settings = EpubViewSettings()

    private val currentChapterSubject = BehaviorSubject.create<Int>()
    private val currentLocationSubject = BehaviorSubject.create<EpubLocation>()

    private val strategyDisposables = CompositeDisposable()
    private var strategy: EpubDisplayStrategy? = null

    var scrollDirection: EpubScrollDirection = EpubScrollDirection.HORIZONTAL_WITH_VERTICAL_CONTENT
        set(scrollDirection) {
            if (field == scrollDirection && strategy != null) {
                return
            }
            field = scrollDirection
            applyScrollDirection(scrollDirection)
        }

    private var savedState: SavedState? = null

    private var urlInterceptor: EpubWebView.UrlInterceptor? = null

    val currentChapter: Int
        get() = currentChapterSubject.value

    val currentLocation: EpubLocation
        get() = currentLocationSubject.value ?: EpubLocation.fromChapter(currentChapter)

    init {
        scrollDirection = EpubScrollDirection.HORIZONTAL_WITH_VERTICAL_CONTENT
    }

    private fun applyScrollDirection(scrollDirection: EpubScrollDirection) {
        // make sure every case is handled
        @Suppress("UNUSED_VARIABLE")
        val unit = when (scrollDirection) {
            EpubScrollDirection.HORIZONTAL_WITH_VERTICAL_CONTENT -> applyDisplayStrategy(HorizontalWithVerticalContentEpubDisplayStrategy())
            EpubScrollDirection.SINGLE_CHAPTER_VERTICAL -> applyDisplayStrategy(SingleChapterVerticalEpubDisplayStrategy())
            EpubScrollDirection.VERTICAL_WITH_VERTICAL_CONTENT -> applyDisplayStrategy(VerticalWithVerticalContentEpubDisplayStrategy())
        }
    }

    private fun applyDisplayStrategy(newStrategy: EpubDisplayStrategy) {
        if (strategy != null) {
            unbindCurrentDisplayStrategy()
        }
        strategy = newStrategy

        newStrategy.bind(this, this)

        newStrategy.currentLocation()
                .doOnNext { location -> currentLocationSubject.onNext(location) }
                .subscribeWith(BaseDisposableObserver())
                .addTo(strategyDisposables)

        newStrategy.onChapterChanged()
                .doOnNext { chapter -> currentChapterSubject.onNext(chapter) }
                .subscribeWith(BaseDisposableObserver())
                .addTo(strategyDisposables)

        if (epub != null) {
            val location = currentLocation
            strategy?.displayEpub(epub, location)
        }
    }

    @JvmOverloads
    fun setEpub(epub: Epub?, location: EpubLocation? = null) {
        @Suppress("NAME_SHADOWING")
        var location = location

        if (epub == null) {
            unbindCurrentDisplayStrategy()
            return
        }

        if (epub.isDestroyed) {
            throw IllegalArgumentException("epub is already destroyed")
        }

        if (strategy == null) {
            applyScrollDirection(scrollDirection)
        }

        if (location == null) {
            location = if (savedState != null && Uri.fromFile(epub.location).toString() == savedState?.epubUri) {
                savedState?.location
            } else {
                EpubLocation.fromChapter(0)
            }
            savedState = null
        }

        if (this.epub === epub) {
            gotoLocation(location)
            return
        }

        this.epub = epub
        strategy?.displayEpub(epub, location)
    }

    private fun unbindCurrentDisplayStrategy() {
        // unbind and remove current strategy
        strategyDisposables.clear()
        strategy?.unbind()
        strategy = null
        removeAllViews()
    }

    fun getEpub(): Epub? {
        return epub
    }

    fun setUrlInterceptor(interceptor: EpubWebView.UrlInterceptor) {
        this.urlInterceptor = interceptor
    }

    fun shouldOverrideUrlLoading(url: String): Boolean {

        if (url.startsWith(Uri.fromFile(epub?.location).toString())) {
            epub?.let { epub ->
                // internal chapter change url
                val spineReferences = epub.book.spine.spineReferences

                for (i in spineReferences.indices) {
                    val spineReference = spineReferences[i]
                    if (url.endsWith(spineReference.resource.href)) {
                        gotoLocation(EpubLocation.fromChapter(i))
                        return true
                    }
                }
            }
            // chapter not found
            // can not open the url
            return true
        }

        if (urlInterceptor?.shouldOverrideUrlLoading(url) == true) {
            return true
        }

        try {
            // try to open url with external app
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        } catch (e: Exception) {
            // ignore
        }

        // we never want to load a new url in the same webview
        return true
    }

    fun gotoLocation(location: EpubLocation?) {
        if (epub == null) {
            throw IllegalStateException("setEpub must be called first")
        }
        strategy?.gotoLocation(location)
    }

    fun currentChapter(): Observable<Int> {
        return currentChapterSubject.distinctUntilChanged()
    }

    fun currentLocation(): Observable<EpubLocation> {
        return currentLocationSubject.distinctUntilChanged()
    }

    /**
     * calls a javascript method on all visible chapters
     * this depends on the selected display strategy
     */
    fun callChapterJavascriptMethod(name: String, vararg args: Any) {
        strategy?.callChapterJavascriptMethod(name, *args)
    }

    /**
     * calls a javascript method on the selected chapter if visible
     * this depends on the selected display strategy
     */
    fun callChapterJavascriptMethod(chapter: Int, name: String, vararg args: Any) {
        strategy?.callChapterJavascriptMethod(chapter, name, *args)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)

        if (epub != null) {
            ss.epubUri = Uri.fromFile(epub?.location).toString()
        }

        ss.location = strategy?.currentLocation ?: currentLocation
        if (savedState != null) {
            ss.location = savedState?.location
        }

        if (ss.location == null && strategy != null) {
            ss.location = EpubLocation.fromChapter(strategy?.currentChapter ?: currentChapter)
        }
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        if (epub != null && Uri.fromFile(epub?.location).toString() == state.epubUri) {
            gotoLocation(state.location)
        } else {
            savedState = state
        }

    }

}
