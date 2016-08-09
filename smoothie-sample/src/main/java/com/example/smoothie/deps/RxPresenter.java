package com.example.smoothie.deps;

import com.example.smoothie.RxMVPActivity;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

@RxMVPActivity.Presenter
public class RxPresenter {

  private final ConnectableObservable<Long> timeObservable;
  private final Subscription connect;

  @Inject
  public RxPresenter() {
    timeObservable = Observable.interval(1, TimeUnit.SECONDS) //
        .subscribeOn(Schedulers.newThread()) //
        .observeOn(AndroidSchedulers.mainThread()) //
        .publish();
    connect = timeObservable.connect();
  }

  public Subscription subscribe(Action1<? super Long> action) {
    return timeObservable.subscribe(action);
  }

  public void stop() {
    connect.unsubscribe();
  }
}
