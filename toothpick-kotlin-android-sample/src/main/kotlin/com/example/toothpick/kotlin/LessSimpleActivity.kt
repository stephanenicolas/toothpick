package com.example.toothpick.kotlin

import android.accounts.AccountManager
import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.FragmentManager
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.toothpick.kotlin.deps.ContextNamer
import javax.inject.Inject
import toothpick.Scope
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule

class LessSimpleActivity : Activity() {

    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    @Inject
    lateinit var alarmManager: AlarmManager

    private lateinit var scope: Scope

    @Inject
    lateinit var contextNamer: ContextNamer
    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.subtitle)
    lateinit var subTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        scope = Toothpick.openScopes(getApplication(), this)
        scope.installModules(SmoothieActivityModule(this))
        super.onCreate(savedInstanceState)
        Toothpick.inject(this, scope)
        setContentView(R.layout.simple_activity)
        ButterKnife.bind(this)
        title.setText(contextNamer.applicationName)
        subTitle.setText(contextNamer.activityName)
    }

    override fun onDestroy() {
        Toothpick.closeScope(this)
        super.onDestroy()
    }
}