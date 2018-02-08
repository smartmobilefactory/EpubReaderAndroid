package com.smartmobilefactory.epubreader.utils

import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver

internal class BaseDisposableObserver<E> : DisposableObserver<E>(), SingleObserver<E> {
    override fun onNext(o: E) {

    }

    override fun onSuccess(e: E) {

    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
    }

    override fun onComplete() {

    }

    fun addTo(compositeDisposable: CompositeDisposable) {
        compositeDisposable.add(this)
    }
}
