package com.smartmobilefactory.epubreader.display.vertical_content.vertical_chapters

import android.support.annotation.Keep
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import com.smartmobilefactory.epubreader.EpubView
import com.smartmobilefactory.epubreader.display.binding.ItemVerticalVerticalContentBinding
import com.smartmobilefactory.epubreader.display.view.EpubWebView
import com.smartmobilefactory.epubreader.display.view.InternalEpubBridge
import com.smartmobilefactory.epubreader.model.Epub
import com.smartmobilefactory.epubreader.model.EpubLocation
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

internal class ChapterAdapter(private val strategy: VerticalWithVerticalContentEpubDisplayStrategy, private val epubView: EpubView) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    private var epub: Epub? = null

    private var locationChapter: Int = 0
    private var tempLocation: EpubLocation? = null
    private var location: EpubLocation? = null

    fun displayEpub(epub: Epub, location: EpubLocation) {
        this.epub = epub
        this.location = location
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val holder = ChapterViewHolder.create(parent)
        holder.binding.webview.bindToSettings(epubView.internalSettings)
        return holder
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {

        val epub = epub ?: return

        val compositeDisposable = CompositeDisposable()

        holder.binding.webview.loadUrl(BLANK_URL)
        holder.binding.webview.setUrlInterceptor { url -> strategy.urlInterceptor(url) }

        val spineReference = epub.book.spine.spineReferences[position]
        holder.binding.webview.loadEpubPage(epub, spineReference, epubView.internalSettings)

        val bridge = Bridge()
        holder.binding.webview.setInternalBridge(bridge)

        handleLocation(position, holder.binding.webview)

        bridge.xPath()
                .doOnNext { xPath ->
                    val location = EpubLocation.fromXPath(strategy.currentChapter, xPath)
                    strategy.currentLocation = location
                }
                .subscribeWith(BaseDisposableObserver())
                .addTo(compositeDisposable)
    }


    private fun handleLocation(position: Int, webView: EpubWebView) {

        if (location == null) {
            return
        }

        val chapter: Int
        if (location is EpubLocation.ChapterLocation) {
            chapter = (location as EpubLocation.ChapterLocation).chapter()
        } else {
            chapter = 0
        }

        if (position == chapter) {
            locationChapter = chapter
            tempLocation = location
            location = null
            webView.isReady()
                    .filter { isReady -> isReady && webView.url != BLANK_URL }
                    .take(1)
                    .delay(1000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        when (tempLocation) {
                            is EpubLocation.IdLocation -> webView.js.getYPositionOfElementWithId((tempLocation as EpubLocation.IdLocation).id())
                            is EpubLocation.XPathLocation -> webView.js.getYPositionOfElementWithXPath((tempLocation as EpubLocation.XPathLocation).xPath())
                            is EpubLocation.RangeLocation -> webView.js.getYPositionOfElementFromRangeStart((tempLocation as EpubLocation.RangeLocation).start())
                        }
                    }
                    .subscribe(BaseDisposableObserver())
        }

    }

    override fun getItemCount(): Int {
        return epub?.book?.spine?.spineReferences?.size ?: 0
    }

    private inner class Bridge : InternalEpubBridge() {

        @Keep
        @JavascriptInterface
        fun resultGetYPositionOfElement(top: Int) {
            val tempLocation = tempLocation ?: return
            val density = epubView.context.resources.displayMetrics.density
            strategy.scrollTo(tempLocation, locationChapter, (top * density).toInt())
        }

    }

    internal class ChapterViewHolder(var binding: ItemVerticalVerticalContentBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {

            fun create(parent: ViewGroup): ChapterViewHolder {
                return ChapterViewHolder(ItemVerticalVerticalContentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    companion object {

        private val BLANK_URL = "about:blank"
    }

}
