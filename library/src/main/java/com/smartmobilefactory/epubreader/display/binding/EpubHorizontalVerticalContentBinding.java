package com.smartmobilefactory.epubreader.display.binding;

import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.R;

import io.reactivex.annotations.NonNull;

public class EpubHorizontalVerticalContentBinding {

    @NonNull
    public final View root;

    @NonNull
    public final ViewPager pager;

    private EpubHorizontalVerticalContentBinding(@NonNull View root) {
        this.root = root;
        this.pager = (ViewPager) root.findViewById(R.id.pager);
    }

    public static EpubHorizontalVerticalContentBinding inflate(LayoutInflater inflater, ViewGroup root, boolean attachToRoot) {
        return new EpubHorizontalVerticalContentBinding(inflater.inflate(R.layout.epub_horizontal_vertical_content, root, attachToRoot));
    }

}
