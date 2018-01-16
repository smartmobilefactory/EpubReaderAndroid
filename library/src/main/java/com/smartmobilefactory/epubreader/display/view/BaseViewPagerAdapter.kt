package com.smartmobilefactory.epubreader.display.view

import android.support.v4.view.PagerAdapter
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup

abstract class BaseViewPagerAdapter : PagerAdapter() {

    private val _attachedViews = SparseArray<View>()

    val attachedViews: List<View>
        get() = (0 until _attachedViews.size()).map { _attachedViews.valueAt(it) }

    fun getViewIfAttached(position: Int): View? {
        return _attachedViews.get(position)
    }

    abstract fun getView(position: Int, parent: ViewGroup): View

    open fun onItemDestroyed(position: Int, view: View) {

    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val view = getView(position, collection)
        _attachedViews.append(position, view)
        collection.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
        _attachedViews.remove(position)
        onItemDestroyed(position, `object`)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}