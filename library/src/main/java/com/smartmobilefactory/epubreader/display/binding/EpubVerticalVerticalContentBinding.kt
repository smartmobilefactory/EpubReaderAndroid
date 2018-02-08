package com.smartmobilefactory.epubreader.display.binding

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.smartmobilefactory.epubreader.R

internal class EpubVerticalVerticalContentBinding private constructor(
        @JvmField
        val root: View
) {

    @JvmField
    val recyclerview: RecyclerView = root.findViewById(R.id.recyclerview) as RecyclerView

    companion object {

        @JvmStatic
        fun inflate(inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean): EpubVerticalVerticalContentBinding {
            return EpubVerticalVerticalContentBinding(inflater.inflate(R.layout.epub_vertical_vertical_content, root, attachToRoot))
        }
    }

}
