package com.example.smoothie.kotlin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import butterknife.ButterKnife
import com.example.smoothie.R
import com.example.smoothie.kotlin.deps.PresenterContextNamer
import toothpick.Scope
import toothpick.Toothpick
import toothpick.kotlin.HasScope
import toothpick.kotlin.androidx.inject
import toothpick.kotlin.injectLazy
import toothpick.smoothie.module.SmoothieActivityModule

class PersistActivity : Activity(), HasScope {
    override lateinit var scope: Scope

    // inject() will use HasScope interface to use to correct scope with presenter scope
    private val contextNamer: PresenterContextNamer by inject()

    private val title by lazy { findViewById<TextView>(R.id.title) }
    private val subTitle by lazy { findViewById<TextView>(R.id.subtitle) }
    private val button by lazy { findViewById<Button>(R.id.hello) }

    override fun onCreate(savedInstanceState: Bundle?) {
        scope = Toothpick.openScopes(application, PRESENTER_SCOPE, this)
        scope.installModules(SmoothieActivityModule(this))
        super.onCreate(savedInstanceState)

        setContentView(R.layout.simple_activity)
        ButterKnife.bind(this)
        title.text = "MVP"
        subTitle.text = contextNamer.instanceCount
        button.visibility = View.GONE
    }

    override fun onDestroy() {
        Toothpick.closeScope(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        //when we leave the presenter flow,
        //we close its scope
        Toothpick.closeScope(PRESENTER_SCOPE)
        super.onBackPressed()
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
