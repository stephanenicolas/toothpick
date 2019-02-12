package com.example.smoothie.kotlin.deps

import android.app.Activity
import android.app.Application
import javax.inject.Inject

class ContextNamer @Inject constructor(
    application: Application,
    activity: Activity
) {
    val applicationName = application::class.simpleName ?: ""
    val activityName = activity::class.simpleName ?: ""
}
