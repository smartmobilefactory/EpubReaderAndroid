package com.smartmobilefactory.epubreader.display.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.smartmobilefactory.epubreader.EpubViewSettings
import com.smartmobilefactory.epubreader.display.EpubDisplayHelper
import com.smartmobilefactory.epubreader.display.WebViewHelper
import com.smartmobilefactory.epubreader.model.Epub
import com.smartmobilefactory.epubreader.model.EpubFont
import com.smartmobilefactory.epubreader.model.EpubLocation
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import nl.siegmann.epublib.domain.SpineReference
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

@Suppress("LeakingThis")
@SuppressLint("SetJavaScriptEnabled")
internal open class EpubWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val settingsCompositeDisposable = CompositeDisposable()
    private val loadEpubCompositeDisposable = CompositeDisposable()

    private var urlInterceptor: (url: String) -> Boolean = { true }

    val webViewHelper = WebViewHelper(this)
    val js = JsApi(webViewHelper)

    private val isReady = BehaviorSubject.createDefault(false)

    private var settingsWeakReference: WeakReference<EpubViewSettings>? = null

    private val client = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            isReady.onNext(false)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            isReady.onNext(true)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return urlInterceptor(url)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return shouldOverrideUrlLoading(view, request.url.toString())
        }

    }

    init {
        setWebViewClient(client)
        settings.javaScriptEnabled = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.allowFileAccess = true
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.TRANSPARENT)
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    fun setInternalBridge(bridge: InternalEpubBridge) {
        addJavascriptInterface(bridge, "internalBridge")
    }

    fun isReady(): Observable<Boolean> {
        return isReady
    }

    fun gotoLocation(location: EpubLocation) {
        isReady.filter { isReady -> isReady }
                .take(1)
                .doOnNext {
                    when (location) {
                        is EpubLocation.IdLocation -> js.scrollToElementById(location.id())
                        is EpubLocation.XPathLocation -> js.scrollToElementByXPath(location.xPath())
                        is EpubLocation.RangeLocation -> js.scrollToRangeStart(location.start())
                    }
                }
                .subscribe(BaseDisposableObserver())
    }

    fun setUrlInterceptor(interceptor: (url: String) -> Boolean) {
        this.urlInterceptor = interceptor
    }

    fun bindToSettings(settings: EpubViewSettings?) {
        if (settings == null) {
            return
        }
        settingsWeakReference = WeakReference(settings)
        settingsCompositeDisposable.clear()

        settings.anySettingHasChanged()
                .doOnNext { setting ->
                    when (setting) {
                        EpubViewSettings.Setting.FONT -> setFont(settings.font)
                        EpubViewSettings.Setting.FONT_SIZE -> setFontSizeSp(settings.fontSizeSp)
                        EpubViewSettings.Setting.JAVASCRIPT_BRIDGE -> setJavascriptBridge(settings.javascriptBridge)
                        else -> {}
                    }
                }
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { js.updateFirstVisibleElement() }
                .subscribeWith(BaseDisposableObserver())
                .addTo(settingsCompositeDisposable)

        setFontSizeSp(settings.fontSizeSp)
        setJavascriptBridge(settings.javascriptBridge)
        setFont(settings.font)

    }

    fun loadEpubPage(epub: Epub, spineReference: SpineReference, settings: EpubViewSettings?) {
        if (settings == null) {
            return
        }
        loadEpubCompositeDisposable.clear()
        settings.anySettingHasChanged()
                // reload html when some settings changed
                .filter { setting -> setting == EpubViewSettings.Setting.CUSTOM_FILES }
                .startWith(EpubViewSettings.Setting.CUSTOM_FILES)
                .flatMap {
                    EpubDisplayHelper.loadHtmlData(this, epub, spineReference, settings)
                            .toObservable<Any>()
                }
                .subscribeWith(BaseDisposableObserver())
                .addTo(loadEpubCompositeDisposable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (settingsWeakReference != null && settingsWeakReference!!.get() != null) {
            bindToSettings(settingsWeakReference!!.get())
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        settingsCompositeDisposable.clear()
        loadEpubCompositeDisposable.clear()
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun setJavascriptBridge(bridge: Any) {
        addJavascriptInterface(bridge, "bridge")
    }

    private fun setFont(font: EpubFont) {
        if (font.uri() == null && font.name() == null) {
            return
        }
        isReady.filter { isReady -> isReady }
                .take(1)
                .subscribe {
                    if (font.uri() == null) {
                        font.name()?.let { js.setFontFamily(it) }
                    } else {
                        js.setFont(font.name(), font.uri())
                    }
                }
    }

    fun callJavascriptMethod(name: String, vararg args: Any) {
        webViewHelper.callJavaScriptMethod(name, *args)
    }

    private fun setFontSizeSp(fontSizeSp: Int) {
        settings.defaultFontSize = fontSizeSp
        settings.minimumFontSize = fontSizeSp
        settings.defaultFixedFontSize = fontSizeSp
        settings.minimumLogicalFontSize = fontSizeSp
    }

}
