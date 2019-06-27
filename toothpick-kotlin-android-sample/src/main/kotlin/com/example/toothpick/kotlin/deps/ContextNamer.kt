package com.example.toothpick.kotlin.deps

import android.app.Activity
import android.app.Application
import javax.inject.Inject

class ContextNamer {

    @Inject
    lateinit var application: Application
    @Inject
    lateinit var activity: Activity

    val applicationName: String
        get() = application::class.java.getSimpleName()

    val activityName: String
        get() = activity::class.java.getSimpleName()
}
