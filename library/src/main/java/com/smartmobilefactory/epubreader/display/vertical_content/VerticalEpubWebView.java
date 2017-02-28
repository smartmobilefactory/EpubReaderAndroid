package com.smartmobilefactory.epubreader.display.vertical_content;

import android.content.Context;
import android.util.AttributeSet;

import com.google.auto.value.AutoValue;

import com.smartmobilefactory.epubreader.display.view.EpubWebView;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class VerticalEpubWebView extends EpubWebView {

    @AutoValue
    public static abstract class ScrollState {
        public abstract int top();
        public abstract int maxTop();
    }

    private BehaviorSubject<ScrollState> currentScrollState = BehaviorSubject.createDefault(new AutoValue_VerticalEpubWebView_ScrollState(0, 100));

    public VerticalEpubWebView(Context context) {
        super(context);
    }

    public VerticalEpubWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalEpubWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        int height =
                (int) Math.floor(getContentHeight() * getScale());
        int webViewHeight = getMeasuredHeight();

        currentScrollState.onNext(new AutoValue_VerticalEpubWebView_ScrollState(t, height - webViewHeight));
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public Observable<ScrollState> verticalScrollState() {
        return currentScrollState;
    }
}
