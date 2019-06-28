package com.example.toothpick.kotlin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.toothpick.kotlin.deps.PresenterContextNamer
import com.example.toothpick.ktp.KTP
import toothpick.Scope
import toothpick.Toothpick
import toothpick.kotlin.inject
import toothpick.smoothie.module.SmoothieActivityModule

class PersistActivity : Activity() {
    private lateinit var scope: Scope

    val contextNamer: PresenterContextNamer by inject()

    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.subtitle)
    lateinit var subTitle: TextView
    @BindView(R.id.hello)
    lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        KTP.openScopes(application, PRESENTER_SCOPE, this)
                .installModules(SmoothieActivityModule(this))
                .inject(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.simple_activity)
        ButterKnife.bind(this)
        title.setText("MVP")
        subTitle.setText(contextNamer.instanceCount)
        button.setVisibility(View.GONE)
    }

    override fun onDestroy() {
        KTP.closeScope(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        //when we leave the presenter flow,
        //we close its scope
        Toothpick.closeScope(PRESENTER_SCOPE)
        super.onBackPressed()
    }

    @javax.inject.Scope
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Presenter

    companion object {
        val PRESENTER_SCOPE = Presenter::class.java
    }
}