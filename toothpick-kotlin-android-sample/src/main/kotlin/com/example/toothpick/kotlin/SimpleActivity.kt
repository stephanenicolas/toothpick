package com.example.toothpick.kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.toothpick.kotlin.deps.ContextNamer
import javax.inject.Inject
import toothpick.Scope
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieAndroidXActivityModule

class SimpleActivity : FragmentActivity() {

    private lateinit var scope: Scope

    @Inject
    lateinit var contextNamer: ContextNamer
    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.subtitle)
    lateinit var subTitle: TextView
    @BindView(R.id.hello)
    lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        scope = Toothpick.openScopes(getApplication(), this)
        scope.installModules(SmoothieAndroidXActivityModule(this))
        super.onCreate(savedInstanceState)
        Toothpick.inject(this, scope)
        setContentView(R.layout.simple_activity)
        ButterKnife.bind(this)
        title.setText(contextNamer.applicationName)
        subTitle.setText(contextNamer.activityName)
        button.setText("click me !")
    }

    @OnClick(R.id.hello)
    @SuppressWarnings("unused")
    internal fun startNewActivity() {
        startActivity(Intent(this, RxMVPActivity::class.java))
    }

    override fun onDestroy() {
        Toothpick.closeScope(this)
        super.onDestroy()
    }
}