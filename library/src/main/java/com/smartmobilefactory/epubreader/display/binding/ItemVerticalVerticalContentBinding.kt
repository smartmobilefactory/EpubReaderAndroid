package com.smartmobilefactory.epubreader.display.binding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.smartmobilefactory.epubreader.R
import com.smartmobilefactory.epubreader.display.view.EpubWebView

internal class ItemVerticalVerticalContentBinding private constructor(
        @JvmField
        val root: View
) {

    @JvmField
    val webview: EpubWebView = root.findViewById(R.id.webview) as EpubWebView

    companion object {

        @JvmStatic
        fun inflate(inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean): ItemVerticalVerticalContentBinding {
            return ItemVerticalVerticalContentBinding(inflater.inflate(R.layout.item_vertical_vertical_content, root, attachToRoot))
        }
    }

}
