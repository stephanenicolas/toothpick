package com.example.toothpick.kotlin.deps

import com.example.toothpick.kotlin.RxMVPActivity
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.observables.ConnectableObservable
import rx.schedulers.Schedulers

@RxMVPActivity.Presenter @Singleton
class RxPresenter {

    private val timeObservable: ConnectableObservable<Long>
    private val connect: Subscription

    init {
        timeObservable = Observable.interval(1, TimeUnit.SECONDS) //
                .subscribeOn(Schedulers.newThread()) //
                .observeOn(AndroidSchedulers.mainThread()) //
                .publish()
        connect = timeObservable.connect()
    }

    fun subscribe(action: Action1<in Long>): Subscription {
        return timeObservable.subscribe(action)
    }

    fun stop() {
        connect.unsubscribe()
    }
}
