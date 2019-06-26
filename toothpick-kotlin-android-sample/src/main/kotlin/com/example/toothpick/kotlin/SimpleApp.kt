package com.example.toothpick.kotlin

import android.app.Application
import toothpick.Scope
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.smoothie.module.SmoothieApplicationModule

class SimpleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Toothpick.setConfiguration(Configuration.forProduction())

        val appScope = Toothpick.openScope(this)
        initToothpick(appScope)
    }

    fun initToothpick(appScope: Scope) {
        appScope.installModules(SmoothieApplicationModule(this))
    }
}
