package com.smartmobilefactory.epubreader.display.binding;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.R;
import com.smartmobilefactory.epubreader.display.view.EpubWebView;

public class ItemVerticalVerticalContentBinding {

    @NonNull
    public final View root;

    @NonNull
    public final EpubWebView webview;

    private ItemVerticalVerticalContentBinding(@NonNull View root) {
        this.root = root;
        this.webview = (EpubWebView) root.findViewById(R.id.webview);
    }

    public static ItemVerticalVerticalContentBinding inflate(LayoutInflater inflater, ViewGroup root, boolean attachToRoot) {
        return new ItemVerticalVerticalContentBinding(inflater.inflate(R.layout.item_vertical_vertical_content, root, attachToRoot));
    }

}
