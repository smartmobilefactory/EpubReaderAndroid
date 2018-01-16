package com.smartmobilefactory.epubreader.display.vertical_content

import android.content.Context
import android.util.AttributeSet

import com.google.auto.value.AutoValue
import com.smartmobilefactory.epubreader.display.view.EpubWebView
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver

import java.util.concurrent.TimeUnit

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

internal class VerticalEpubWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : EpubWebView(context, attrs, defStyleAttr) {

    private val compositeDisposable = CompositeDisposable()

    private val currentScrollState = BehaviorSubject.createDefault<ScrollState>(ScrollState(0, 100))

    data class ScrollState(
            val top: Int,
            val maxTop: Int
    )

    init {
        verticalScrollState()
                .sample(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (progress == 100) {
                        callJavascriptMethod("updateFirstVisibleElement")
                    }
                }
                .subscribeWith(BaseDisposableObserver())
                .addTo(compositeDisposable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {

        val height = Math.floor((contentHeight * scale).toDouble()).toInt()
        val webViewHeight = measuredHeight

        currentScrollState.onNext(ScrollState(t, height - webViewHeight))
        super.onScrollChanged(l, t, oldl, oldt)
    }

    fun verticalScrollState(): Observable<ScrollState> {
        return currentScrollState
    }
}
