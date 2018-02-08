package com.smartmobilefactory.epubreader.display.vertical_content.horizontal_chapters

import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.smartmobilefactory.epubreader.EpubView
import com.smartmobilefactory.epubreader.display.EpubDisplayStrategy
import com.smartmobilefactory.epubreader.display.binding.EpubHorizontalVerticalContentBinding
import com.smartmobilefactory.epubreader.display.view.EpubWebView
import com.smartmobilefactory.epubreader.model.Epub
import com.smartmobilefactory.epubreader.model.EpubLocation

internal class HorizontalWithVerticalContentEpubDisplayStrategy : EpubDisplayStrategy() {

    private lateinit var binding: EpubHorizontalVerticalContentBinding

    private lateinit var epubView: EpubView
    private lateinit var pagerAdapter: PagerAdapter

    internal val urlInterceptor: (String) -> Boolean = { url: String -> epubView.shouldOverrideUrlLoading(url) }

    override fun bind(epubView: EpubView, parent: ViewGroup) {
        this.epubView = epubView
        val inflater = LayoutInflater.from(parent.context)
        binding = EpubHorizontalVerticalContentBinding.inflate(inflater, parent, true)

        binding.pager.offscreenPageLimit = 1
        binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                currentChapter = position
                val location = pagerAdapter.getChapterLocation(position)
                        ?: EpubLocation.fromChapter(position)
                currentLocation = location
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        pagerAdapter = PagerAdapter(this, epubView)
        binding.pager.adapter = pagerAdapter
    }

    override fun displayEpub(epub: Epub, location: EpubLocation) {
        pagerAdapter.displayEpub(epub, location)

        if (location is EpubLocation.ChapterLocation) {
            binding.pager.currentItem = location.chapter()
        }

    }

    override fun gotoLocation(location: EpubLocation) {
        if (location is EpubLocation.ChapterLocation) {

            val binding = pagerAdapter.getViewBindingIfAttached(location.chapter())
            if (binding != null) {
                binding.webview.gotoLocation(location)
            } else {
                displayEpub(epubView.getEpub()!!, location)
            }

            this.binding.pager.currentItem = location.chapter()
            currentLocation = location
        }
    }

    override fun callChapterJavascriptMethod(chapter: Int, name: String, vararg args: Any) {
        val binding = pagerAdapter.getViewBindingIfAttached(chapter)
        binding?.webview?.callJavascriptMethod(name, *args)
    }

    override fun callChapterJavascriptMethod(name: String, vararg args: Any) {
        for (binding in pagerAdapter.attachedViewBindings) {
            binding.webview.callJavascriptMethod(name, *args)
        }
    }

}
