package com.smartmobilefactory.epubreader

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class EpubViewPlugin {

    private val dataChangedSubject = PublishSubject.create<Any>()

    open val customChapterCss: List<String> = listOf()

    open val customChapterScripts: List<String> = listOf()

    open val javascriptBridge: EpubJavaScriptBridge? = null

    internal fun dataChanged(): Observable<Any> = dataChangedSubject

    fun notifyDataChanged() {
        dataChangedSubject.onNext(Any())
    }

}

data class EpubJavaScriptBridge(
        val name: String,
        val bridge: Any
)
