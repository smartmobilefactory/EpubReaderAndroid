package com.smartmobilefactory.epubreader.display.binding;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.R;

public class EpubVerticalVerticalContentBinding {

    @NonNull
    public final View root;

    @NonNull
    public final RecyclerView recyclerview;

    private EpubVerticalVerticalContentBinding(@NonNull View root) {
        this.root = root;
        this.recyclerview = (RecyclerView) root.findViewById(R.id.recyclerview);
    }

    public static EpubVerticalVerticalContentBinding inflate(LayoutInflater inflater, ViewGroup root, boolean attachToRoot) {
        return new EpubVerticalVerticalContentBinding(inflater.inflate(R.layout.epub_vertical_vertical_content, root, attachToRoot));
    }

}
