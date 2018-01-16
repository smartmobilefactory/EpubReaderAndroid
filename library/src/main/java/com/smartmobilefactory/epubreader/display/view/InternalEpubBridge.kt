package com.smartmobilefactory.epubreader.display.view

import android.support.annotation.Keep
import android.webkit.JavascriptInterface

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

open class InternalEpubBridge {

    private val xPathSubject = PublishSubject.create<String>()

    @Keep
    @JavascriptInterface
    fun onLocationChanged(xPath: String?) {
        if (xPath == null) {
            return
        }
        xPathSubject.onNext(xPath)
    }

    fun xPath(): Observable<String> {
        return xPathSubject.distinctUntilChanged()
    }

}
