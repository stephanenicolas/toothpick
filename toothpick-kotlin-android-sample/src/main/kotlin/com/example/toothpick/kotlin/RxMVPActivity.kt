package com.example.toothpick.kotlin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.util.Log
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.toothpick.kotlin.deps.RxPresenter
import com.example.toothpick.ktp.KTP
import javax.inject.Inject
import rx.Subscription
import toothpick.Scope
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule
import rx.functions.Action1
import toothpick.smoothie.module.SmoothieAndroidXActivityModule

class RxMVPActivity : Activity() {
    private lateinit var scope: Scope

    val rxPresenter: RxPresenter by KTP.inject()

    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.subtitle)
    lateinit var subTitle: TextView
    @BindView(R.id.hello)
    lateinit var button: Button
    private lateinit var subscription: Subscription

    override fun onCreate(savedInstanceState: Bundle?) {
        KTP.openScopes(application, PRESENTER_SCOPE, this)
                .installModules(SmoothieActivityModule(this))
                .inject(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.simple_activity)
        ButterKnife.bind(this)
        title.setText("MVP")
        subscription = rxPresenter.subscribe(Action1 { aLong -> subTitle.text = aLong.toString() })
        button.setVisibility(View.GONE)
    }

    override fun onDestroy() {
        Log.d("", Toothpick.openScope(application).toString())
        KTP.closeScope(this)
        subscription.unsubscribe()
        if (isFinishing()) {
            //when we leave the presenter flow,
            //we close its scope
            rxPresenter.stop()
            KTP.closeScope(PRESENTER_SCOPE)
            Toothpick.closeScope(PRESENTER_SCOPE)
        }
        super.onDestroy()
    }

    @javax.inject.Scope
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Presenter

    companion object {
        val PRESENTER_SCOPE = Presenter::class.java
    }
}
