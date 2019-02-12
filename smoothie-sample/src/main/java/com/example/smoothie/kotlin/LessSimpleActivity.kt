package com.example.smoothie.kotlin

import android.accounts.AccountManager
import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import butterknife.ButterKnife
import com.example.smoothie.R
import com.example.smoothie.kotlin.deps.ContextNamer
import toothpick.Toothpick
import toothpick.kotlin.androidx.inject
import toothpick.kotlin.module
import toothpick.kotlin.providedBy
import toothpick.smoothie.module.SmoothieActivityModule

class LessSimpleActivity : Activity() {
    // inject() will attempt to find a scope, then default to this activity instance
    private val injectedApplication: Application by inject()
    private val accountManager: AccountManager by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private val alarmManager: AlarmManager by inject()
    private val injectFragmentManager: FragmentManager by inject()
    private val activity: Activity by inject()
    private val contextNamer: ContextNamer by inject()
    private val specialString: String by inject(name = "SpecialString")

    private val title by lazy { findViewById<TextView>(R.id.title) }
    private val subTitle by lazy { findViewById<TextView>(R.id.subtitle) }

    override fun onCreate(savedInstanceState: Bundle?) {
        val scope = Toothpick.openScopes(application, this)
        scope.installModules(SmoothieActivityModule(this))
        scope.installModules(module { String::class named "SpecialString" providedBy "My Special String" })
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_activity)
        ButterKnife.bind(this)
        title.text = "${contextNamer.applicationName} with $specialString"
        subTitle.text = contextNamer.activityName
    }

    override fun onDestroy() {
        Toothpick.closeScope(this)
        super.onDestroy()
    }
}
