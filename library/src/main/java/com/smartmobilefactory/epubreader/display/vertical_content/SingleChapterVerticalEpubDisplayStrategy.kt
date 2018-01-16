package com.smartmobilefactory.epubreader.display.vertical_content

import android.view.LayoutInflater
import android.view.ViewGroup

import com.smartmobilefactory.epubreader.EpubView
import com.smartmobilefactory.epubreader.EpubViewSettings
import com.smartmobilefactory.epubreader.display.EpubDisplayStrategy
import com.smartmobilefactory.epubreader.display.binding.ItemEpubVerticalContentBinding
import com.smartmobilefactory.epubreader.display.view.InternalEpubBridge
import com.smartmobilefactory.epubreader.model.Epub
import com.smartmobilefactory.epubreader.model.EpubLocation
import nl.siegmann.epublib.domain.SpineReference

internal class SingleChapterVerticalEpubDisplayStrategy : EpubDisplayStrategy() {
    private lateinit var epubView: EpubView
    private lateinit var binding: ItemEpubVerticalContentBinding

    private var settings: EpubViewSettings? = null

    override fun bind(epubView: EpubView, parent: ViewGroup) {
        this.epubView = epubView
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemEpubVerticalContentBinding.inflate(inflater, parent, true)
        settings = epubView.settings
    }

    override fun displayEpub(epub: Epub, location: EpubLocation) {
        if (location is EpubLocation.ChapterLocation) {
            val chapter = location.chapter()
            val spineReference = epub.book.spine.spineReferences[chapter]
            binding.webview.loadEpubPage(epub, spineReference, epubView.settings)
            currentChapter = chapter
        }

        binding.webview.gotoLocation(location)
        binding.webview.setUrlInterceptor { url -> epubView.shouldOverrideUrlLoading(url) }
        binding.webview.bindToSettings(settings)

        val bridge = InternalEpubBridge()
        binding.webview.setInternalBridge(bridge)

        bridge.xPath().subscribe { xPath -> currentLocation = EpubLocation.fromXPath(currentChapter, xPath) }

        VerticalContentBinderHelper.bind(binding)
    }

    override fun gotoLocation(location: EpubLocation) {
        if (location is EpubLocation.ChapterLocation) {
            if (currentChapter == location.chapter()) {
                binding.webview.gotoLocation(location)
            } else {
                epubView.getEpub()?.let { displayEpub(it, location) }
            }
            currentLocation = location
        }
    }

    override fun callChapterJavascriptMethod(chapter: Int, name: String, vararg args: Any) {
        if (chapter == currentChapter) {
            callChapterJavascriptMethod(name, *args)
        }
    }

    override fun callChapterJavascriptMethod(name: String, vararg args: Any) {
        binding.webview.callJavascriptMethod(name, *args)
    }
}
