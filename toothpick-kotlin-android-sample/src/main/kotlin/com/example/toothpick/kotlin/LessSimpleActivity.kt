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
import com.example.toothpick.ktp.KTP
import javax.inject.Inject
import toothpick.Scope
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieActivityModule

class LessSimpleActivity : Activity() {

    val accountManager: AccountManager by KTP.inject()
    val sharedPreferences: SharedPreferences by KTP.inject()
    val alarmManager: AlarmManager by KTP.inject()

    private lateinit var scope: Scope

    val contextNamer: ContextNamer by KTP.inject()

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