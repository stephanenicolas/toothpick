package com.example.smoothie.kotlin.deps

import com.example.smoothie.kotlin.PersistActivity
import javax.inject.Inject

@PersistActivity.Presenter
class PresenterContextNamer @Inject constructor() {

    init {
        countInstances++
    }

    val instanceCount: String
        get() = "Instance# $countInstances"

    companion object {
        private var countInstances = 0
    }
}
