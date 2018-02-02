package com.smartmobilefactory.epubreader.display.vertical_content.vertical_chapters

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.smartmobilefactory.epubreader.EpubView
import com.smartmobilefactory.epubreader.UrlInterceptor
import com.smartmobilefactory.epubreader.display.EpubDisplayStrategy
import com.smartmobilefactory.epubreader.display.binding.EpubVerticalVerticalContentBinding
import com.smartmobilefactory.epubreader.display.view.EpubWebView
import com.smartmobilefactory.epubreader.model.Epub
import com.smartmobilefactory.epubreader.model.EpubLocation
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver

import java.util.concurrent.TimeUnit

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

internal class VerticalWithVerticalContentEpubDisplayStrategy : EpubDisplayStrategy() {

    private lateinit var binding: EpubVerticalVerticalContentBinding

    private lateinit var epubView: EpubView

    private lateinit var chapterAdapter: ChapterAdapter

    private val scrollPosition = PublishSubject.create<Pair<Int, Int>>()

    internal val urlInterceptor: (String) -> Boolean = { url: String -> epubView.shouldOverrideUrlLoading(url) }

    override fun bind(epubView: EpubView, parent: ViewGroup) {
        this.epubView = epubView
        val inflater = LayoutInflater.from(parent.context)
        binding = EpubVerticalVerticalContentBinding.inflate(inflater, parent, true)

        val layoutManager = LinearLayoutManager(epubView.context)
        layoutManager.setInitialPrefetchItemCount(2)
        binding.recyclerview.layoutManager = layoutManager
        chapterAdapter = ChapterAdapter(this, epubView)
        binding.recyclerview.adapter = chapterAdapter
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val firstVisibleView = layoutManager.findViewByPosition(firstVisibleItemPosition)
                scrollPosition.onNext(Pair(firstVisibleItemPosition, firstVisibleView.top))
                currentChapter = firstVisibleItemPosition
            }
        })

        scrollPosition
                .sample(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { positionTopPair ->
                    val holder = binding.recyclerview.findViewHolderForAdapterPosition(positionTopPair.first) as? ChapterAdapter.ChapterViewHolder
                    if (holder != null) {
                        val density = epubView.context.resources.displayMetrics.density
                        holder.binding.webview.js.updateFirstVisibleElementByTopPosition(-positionTopPair.second / density)
                    }
                }
                .subscribe(BaseDisposableObserver())
    }

    override fun displayEpub(epub: Epub, location: EpubLocation) {
        chapterAdapter.displayEpub(epub, location)

        if (location is EpubLocation.ChapterLocation) {
            binding.recyclerview.scrollToPosition(location.chapter())
        }
    }

    override fun gotoLocation(location: EpubLocation) {
        if (location is EpubLocation.ChapterLocation) {

            this.binding.recyclerview.scrollToPosition(location.chapter())
            displayEpub(epubView.getEpub()!!, location)
            currentLocation = location
        }
    }

    override fun callChapterJavascriptMethod(chapter: Int, name: String, vararg args: Any) {
        if (chapterAdapter == null) {
            return
        }
        val holder = binding.recyclerview.findViewHolderForAdapterPosition(chapter) as ChapterAdapter.ChapterViewHolder
        holder.binding.webview.callJavascriptMethod(name, *args)
    }

    override fun callChapterJavascriptMethod(name: String, vararg args: Any) {
        callChapterJavascriptMethod(currentChapter, name, *args)
    }

    internal fun scrollTo(location: EpubLocation, chapter: Int, offsetY: Int) {
        binding.recyclerview.post {
            val linearLayoutManager = binding.recyclerview.layoutManager as LinearLayoutManager
            linearLayoutManager.scrollToPositionWithOffset(chapter, -offsetY)
            currentLocation = location
        }
    }

}
