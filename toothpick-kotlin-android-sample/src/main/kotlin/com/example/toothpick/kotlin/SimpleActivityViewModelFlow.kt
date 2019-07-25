package com.example.toothpick.kotlin

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import toothpick.ktp.KTPScope
import toothpick.smoothie.viewmodel.closeOnViewModelCleared
import toothpick.smoothie.viewmodel.installViewModelBinding

class SimpleActivityViewModelFlow {

    companion object {
        private val viewModelScopeName = SimpleActivityViewModelFlow.Scope::class.java

        fun openScope(parentScope: KTPScope, factory: ViewModelProvider.Factory? = null, activity: FragmentActivity): KTPScope {
            return parentScope.openSubScope(viewModelScopeName) {
                (it as KTPScope)
                        .closeOnViewModelCleared(activity)
                        .installViewModelBinding<SimpleActivityViewModel>(activity, factory)
            }
        }
    }

    @javax.inject.Scope
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Scope
}

fun KTPScope.openSimpleActivityViewModelSubScope(activity: FragmentActivity, factory: ViewModelProvider.Factory? = null): KTPScope {
    return SimpleActivityViewModelFlow.openScope(this, factory, activity)
}