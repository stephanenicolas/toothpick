package com.example.toothpick.kotlin

import android.accounts.AccountManager
import android.app.Activity
import android.app.AlarmManager
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.toothpick.kotlin.deps.ContextNamer
import com.example.toothpick.ktp.KTP
import toothpick.kotlin.inject
import toothpick.smoothie.module.SmoothieActivityModule

class LessSimpleActivity : Activity() {

    val accountManager: AccountManager by inject()
    val sharedPreferences: SharedPreferences by inject()
    val alarmManager: AlarmManager by inject()
    val contextNamer: ContextNamer by inject()

    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.subtitle)
    lateinit var subTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        KTP.openScopes(application, this)
                .installModules(SmoothieActivityModule(this))
                .inject(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.simple_activity)
        ButterKnife.bind(this)
        title.setText(contextNamer.applicationName)
        subTitle.setText(contextNamer.activityName)
    }

    override fun onDestroy() {
        KTP.closeScope(this)
        super.onDestroy()
    }
}