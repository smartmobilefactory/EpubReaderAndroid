package com.smartmobilefactory.epubreader.utils;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class BaseDisposableObserver<E> extends DisposableObserver<E> implements SingleObserver<E> {
    @Override
    public void onNext(E o) {

    }

    @Override
    public void onSuccess(E e) {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }

    public void addTo(CompositeDisposable compositeDisposable) {
        compositeDisposable.add(this);
    }
}
