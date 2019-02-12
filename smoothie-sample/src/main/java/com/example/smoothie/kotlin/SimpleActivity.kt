package com.example.smoothie.kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.smoothie.R
import com.example.smoothie.kotlin.deps.ContextNamer
import toothpick.Toothpick
import toothpick.kotlin.androidx.inject
import toothpick.smoothie.module.SmoothieAndroidXActivityModule

class SimpleActivity : FragmentActivity() {
    // Will use activity instance as scope
    private val contextNamer: ContextNamer by inject()

    private val title by lazy { findViewById<TextView>(R.id.title) }
    private val subTitle by lazy { findViewById<TextView>(R.id.subtitle) }
    private val button by lazy { findViewById<Button>(R.id.hello) }

    override fun onCreate(savedInstanceState: Bundle?) {
        val scope = Toothpick.openScopes(application, this)
        scope.installModules(SmoothieAndroidXActivityModule(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_activity)

        title.text = contextNamer.applicationName
        subTitle.text = contextNamer.activityName
        button.apply {
            text = "click me !"
            setOnClickListener { startActivity(Intent(this@SimpleActivity, RxMVPActivity::class.java)) }
        }
    }

    override fun onDestroy() {
        Toothpick.closeScope(this)
        super.onDestroy()
    }
}
