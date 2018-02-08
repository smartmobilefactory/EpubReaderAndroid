package com.smartmobilefactory.epubreader.display.binding

import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.smartmobilefactory.epubreader.R

import io.reactivex.annotations.NonNull

internal class EpubHorizontalVerticalContentBinding private constructor(
        @JvmField
        val root: View
) {

    @JvmField
    val pager: ViewPager = root.findViewById(R.id.pager) as ViewPager

    companion object {

        @JvmStatic
        fun inflate(inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean): EpubHorizontalVerticalContentBinding {
            return EpubHorizontalVerticalContentBinding(inflater.inflate(R.layout.epub_horizontal_vertical_content, root, attachToRoot))
        }
    }

}
