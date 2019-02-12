package com.example.smoothie.kotlin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.smoothie.R
import com.example.smoothie.kotlin.deps.RxPresenter
import rx.Subscription
import rx.functions.Action1
import toothpick.Scope
import toothpick.Toothpick
import toothpick.kotlin.injectLazy
import toothpick.smoothie.module.SmoothieActivityModule

class RxMVPActivity : Activity() {
    private lateinit var activityScope: Scope

    // Uses lazy assign of scope via InjectConfig block and default TP Kotlin Extension
    private val rxPresenter: RxPresenter by injectLazy { scope = activityScope }

    private val title by lazy { findViewById<TextView>(R.id.title) }
    private val subTitle by lazy { findViewById<TextView>(R.id.subtitle) }
    private val button by lazy { findViewById<Button>(R.id.hello) }

    private lateinit var subscription: Subscription

    override fun onCreate(savedInstanceState: Bundle?) {
        activityScope = Toothpick.openScopes(application, PRESENTER_SCOPE, this)
        activityScope.installModules(SmoothieActivityModule(this))
        super.onCreate(savedInstanceState)

        setContentView(R.layout.simple_activity)
        title.text = "MVP"
        subscription = rxPresenter.subscribe(Action1 { aLong -> subTitle.text = aLong.toString() } )
        button.visibility = View.GONE
    }

    override fun onDestroy() {
        Toothpick.closeScope(this)
        subscription.unsubscribe()
        if (isFinishing) {
            //when we leave the presenter flow,
            //we close its scope
            rxPresenter.stop()
            Toothpick.closeScope(PRESENTER_SCOPE)
        }
        super.onDestroy()
    }

    @Target(AnnotationTarget.CLASS)
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    @javax.inject.Scope
    annotation class Presenter

    companion object {
        val PRESENTER_SCOPE: Class<*> = Presenter::class.java
    }
}
