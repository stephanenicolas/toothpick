package com.example.toothpick

import android.app.Application
import com.example.toothpick.annotation.ApplicationScope
import toothpick.Scope
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module

class BackpackApplication : Application() {

    lateinit var scope: Scope

    override fun onCreate() {
        super.onCreate()

        scope = KTP.openScope(ApplicationScope::class.java)
                .installModules(module {
                    bind<Application>().toInstance { this@BackpackApplication }
                })
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        scope.release()
    }
}
