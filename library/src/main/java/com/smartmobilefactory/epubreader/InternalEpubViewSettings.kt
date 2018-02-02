package com.smartmobilefactory.epubreader

import android.os.Handler
import android.os.Looper
import com.smartmobilefactory.epubreader.model.EpubFont
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

internal class InternalEpubViewSettings(
        private val settings: EpubViewSettings
) {

    private val settingsChangedSubject = PublishSubject.create<EpubViewSettings.Setting>()
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private val disposables = mutableMapOf<EpubViewPlugin, Disposable>()

    private val plugins = mutableListOf<EpubViewPlugin>()

    val fontSizeSp: Int
        get() = settings.fontSizeSp

    val font: EpubFont
        get() = settings.font

    val customChapterCss: List<String>
        get() = mutableSetOf<String>().apply {
            addAll(settings.customChapterCss)
            plugins.forEach { plugin ->
                addAll(plugin.customChapterCss)
            }
        }.toList()

    val customChapterScripts: List<String>
        get() = mutableSetOf<String>().apply {
            addAll(settings.customChapterScripts)
            plugins.forEach { plugin ->
                addAll(plugin.customChapterScripts)
            }
        }.toList()

    val javascriptBridges: List<EpubJavaScriptBridge>
        get() = mutableListOf<EpubJavaScriptBridge>().apply {
            if (settings.javascriptBridge != null) {
                add(EpubJavaScriptBridge("bridge", settings.javascriptBridge))
            }
            plugins.forEach { plugin ->
                plugin.javascriptBridge?.let { add(it) }
            }
        }

    fun addPlugin(epubPlugin: EpubViewPlugin) {
        if (!plugins.contains(epubPlugin)) {
            plugins.add(epubPlugin)
            val disposable = epubPlugin
                    .dataChanged()
                    .subscribe {
                        onSettingHasChanged(setting = EpubViewSettings.Setting.CUSTOM_FILES)
                    }
            disposables[epubPlugin] = disposable
        }
    }

    fun removePlugin(epubPlugin: EpubViewPlugin) {
        plugins.remove(epubPlugin)
        disposables[epubPlugin]?.dispose()
        disposables.remove(epubPlugin)
    }

    private fun onSettingHasChanged(setting: EpubViewSettings.Setting) {
        // make sure the values only updates on the main thread but avoid delaying events
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            settingsChangedSubject.onNext(setting)
        } else {
            mainThreadHandler.post({ settingsChangedSubject.onNext(setting) })
        }
    }

    fun anySettingHasChanged(): Observable<EpubViewSettings.Setting> {
        return settingsChangedSubject.mergeWith(settings.anySettingHasChanged())
    }

}
