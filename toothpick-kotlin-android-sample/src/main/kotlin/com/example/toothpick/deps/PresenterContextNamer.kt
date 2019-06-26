package com.example.toothpick.kotlin.deps

import android.app.Application
import com.example.toothpick.kotlin.PersistActivity
import javax.inject.Inject

@PersistActivity.Presenter
class PresenterContextNamer {

    @Inject
    lateinit var application: Application

    val instanceCount: String
        get() = "Instance# $countInstances"

    init {
        countInstances++
    }

    companion object {
        private var countInstances = 0
    }
}
