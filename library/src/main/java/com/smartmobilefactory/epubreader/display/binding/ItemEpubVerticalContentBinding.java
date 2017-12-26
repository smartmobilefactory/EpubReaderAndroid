package com.smartmobilefactory.epubreader.display.binding;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.smartmobilefactory.epubreader.R;
import com.smartmobilefactory.epubreader.display.vertical_content.VerticalEpubWebView;

public class ItemEpubVerticalContentBinding {

    @NonNull
    public final View root;
    @NonNull
    public final ProgressBar progressBar;
    @NonNull
    public final VerticalSeekBar seekbar;
    @NonNull
    public final VerticalEpubWebView webview;

    private ItemEpubVerticalContentBinding(@NonNull View root) {
        this.root = root;
        this.progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        this.seekbar = (VerticalSeekBar) root.findViewById(R.id.seekbar);
        this.webview = (VerticalEpubWebView) root.findViewById(R.id.webview);
    }

    public static ItemEpubVerticalContentBinding inflate(LayoutInflater inflater, ViewGroup root, boolean attachToRoot) {
        return new ItemEpubVerticalContentBinding(inflater.inflate(R.layout.item_epub_vertical_content, root, attachToRoot));
    }

    public static ItemEpubVerticalContentBinding bind(View root) {
        return new ItemEpubVerticalContentBinding(root);
    }
}
