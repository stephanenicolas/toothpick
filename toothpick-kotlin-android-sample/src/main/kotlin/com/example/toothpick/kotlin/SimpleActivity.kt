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
import com.example.toothpick.ktp.KTP
import toothpick.Scope
import toothpick.kotlin.inject
import toothpick.smoothie.module.SmoothieAndroidXActivityModule

class SimpleActivity : FragmentActivity() {

    private lateinit var scope: Scope

    val contextNamer: ContextNamer by inject()

    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.subtitle)
    lateinit var subTitle: TextView
    @BindView(R.id.hello)
    lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        KTP.openScopes(application, this)
                .installModules(SmoothieAndroidXActivityModule(this))
                .inject(this)

        super.onCreate(savedInstanceState)

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
        KTP.closeScope(this)
        super.onDestroy()
    }
}