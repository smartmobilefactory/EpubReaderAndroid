package com.smartmobilefactory.epubreader.display.view;

import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseViewPagerAdapter extends PagerAdapter {

    private final SparseArray<View> attachedViews = new SparseArray<>();

    @Nullable
    public View getViewIfAttached(int position) {
        return attachedViews.get(position);
    }

    public List<View> getAttachedViews() {
        List<View> views = new ArrayList<>();
        for (int i = 0; i < attachedViews.size(); i++) {
            views.add(attachedViews.valueAt(i));
        }
        return views;
    }

    public abstract View getView(int position, ViewGroup parent);

    public void onItemDestroyed(int position, View view) {

    }

    public Object instantiateItem(ViewGroup collection, int position) {
        View view = getView(position, collection);
        attachedViews.append(position, view);
        collection.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        attachedViews.remove(position);
        onItemDestroyed(position, (View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}