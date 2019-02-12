package com.example.smoothie.kotlin.deps

import com.example.smoothie.kotlin.RxMVPActivity
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.observables.ConnectableObservable
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RxMVPActivity.Presenter
class RxPresenter @Inject constructor() {

    private val timeObservable: ConnectableObservable<Long> = Observable.interval(1, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .publish()

    private val connect = timeObservable.connect()


    fun subscribe(action: Action1<Long>): Subscription {
        return timeObservable.subscribe(action)
    }

    fun stop() {
        connect.unsubscribe()
    }
}
