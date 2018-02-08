package com.smartmobilefactory.epubreader.display.binding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar
import com.smartmobilefactory.epubreader.R
import com.smartmobilefactory.epubreader.display.vertical_content.VerticalEpubWebView

internal class ItemEpubVerticalContentBinding private constructor(
        @JvmField
        val root: View
) {

    @JvmField
    val progressBar: ProgressBar = root.findViewById(R.id.progressBar) as ProgressBar

    @JvmField
    val seekbar: VerticalSeekBar = root.findViewById(R.id.seekbar) as VerticalSeekBar

    @JvmField
    val webview: VerticalEpubWebView = root.findViewById(R.id.webview) as VerticalEpubWebView

    companion object {

        @JvmStatic
        fun inflate(inflater: LayoutInflater, root: ViewGroup, attachToRoot: Boolean): ItemEpubVerticalContentBinding {
            return ItemEpubVerticalContentBinding(inflater.inflate(R.layout.item_epub_vertical_content, root, attachToRoot))
        }

        @JvmStatic
        fun bind(root: View): ItemEpubVerticalContentBinding {
            return ItemEpubVerticalContentBinding(root)
        }
    }
}
