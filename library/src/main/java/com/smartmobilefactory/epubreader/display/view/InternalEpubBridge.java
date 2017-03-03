package com.smartmobilefactory.epubreader.display.view;

import android.support.annotation.Keep;
import android.webkit.JavascriptInterface;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class InternalEpubBridge {

    private PublishSubject<String> xPathSubject = PublishSubject.create();

    @Keep
    @JavascriptInterface
    public void onLocationChanged(String xPath) {
        if (xPath == null) {
            return;
        }
        xPathSubject.onNext(xPath);
    }

    public Observable<String> xPath() {
        return xPathSubject.distinctUntilChanged();
    }

}
