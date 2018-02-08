package com.smartmobilefactory.epubreader.display.vertical_content.horizontal_chapters

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smartmobilefactory.epubreader.EpubView
import com.smartmobilefactory.epubreader.display.binding.ItemEpubVerticalContentBinding
import com.smartmobilefactory.epubreader.display.vertical_content.VerticalContentBinderHelper
import com.smartmobilefactory.epubreader.display.view.BaseViewPagerAdapter
import com.smartmobilefactory.epubreader.display.view.EpubWebView
import com.smartmobilefactory.epubreader.display.view.InternalEpubBridge
import com.smartmobilefactory.epubreader.model.Epub
import com.smartmobilefactory.epubreader.model.EpubLocation
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver
import io.reactivex.disposables.CompositeDisposable

internal class PagerAdapter(private val strategy: HorizontalWithVerticalContentEpubDisplayStrategy, private val epubView: EpubView) : BaseViewPagerAdapter() {

    private val chapterDisposables = SparseArray<CompositeDisposable>()
    private val chapterLocations = SparseArray<EpubLocation>()

    private var epub: Epub? = null
    private var location: EpubLocation? = null

    val attachedViewBindings: List<ItemEpubVerticalContentBinding>
        get() = attachedViews.map { ItemEpubVerticalContentBinding.bind(it) }

    fun displayEpub(epub: Epub, location: EpubLocation) {
        this.epub = epub
        this.location = location
        notifyDataSetChanged()
    }

    override fun getView(position: Int, parent: ViewGroup): View {

        val compositeDisposable = chapterDisposables.get(position) ?: run {
            chapterDisposables.append(position, CompositeDisposable())
            chapterDisposables.get(position)
        }

        val binding = ItemEpubVerticalContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val epub = epub ?: return binding.root

        binding.webview.setUrlInterceptor { strategy.urlInterceptor(it) }

        val spineReference = epub.book.spine.spineReferences[position]
        binding.webview.loadEpubPage(epub, spineReference, epubView.settings)

        handleLocation(position, binding.webview)

        binding.webview.bindToSettings(epubView.settings)

        val bridge = InternalEpubBridge()
        binding.webview.setInternalBridge(bridge)

        bridge.xPath()
                .doOnNext { xPath ->
                    val location = EpubLocation.fromXPath(strategy.currentChapter, xPath)
                    chapterLocations.append(position, location)
                    strategy.currentLocation = location
                }
                .subscribeWith(BaseDisposableObserver())
                .addTo(compositeDisposable)

        compositeDisposable.add(VerticalContentBinderHelper.bind(binding))

        return binding.root
    }

    fun getChapterLocation(position: Int): EpubLocation? {
        return chapterLocations.get(position)
    }

    fun getViewBindingIfAttached(position: Int): ItemEpubVerticalContentBinding? {
        val view = getViewIfAttached(position) ?: return null
        return ItemEpubVerticalContentBinding.bind(view)
    }

    private fun handleLocation(position: Int, webView: EpubWebView) {

        if (location == null) {
            return
        }

        val chapter: Int = if (location is EpubLocation.ChapterLocation) {
            (location as EpubLocation.ChapterLocation).chapter()
        } else {
            0
        }

        if (position == chapter) {
            location?.let { webView.gotoLocation(it) }
            location = null
        }

    }

    override fun onItemDestroyed(position: Int, view: View) {
        super.onItemDestroyed(position, view)
        chapterLocations.delete(position)
        val compositeDisposable = chapterDisposables.get(position)
        compositeDisposable?.clear()
    }

    override fun getCount(): Int {
        return epub?.book?.spine?.spineReferences?.size ?: 0
    }

    override fun getItemPosition(any: Any?): Int {
        return POSITION_NONE
    }

}
