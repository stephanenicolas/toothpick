package com.example.toothpick.kotlin

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import toothpick.smoothie.viewmodel.closeOnViewModelCleared
import toothpick.smoothie.viewmodel.installViewModelBinding

class SimpleActivityViewModelFlow {

    companion object {
        private val viewModelScopeName = SimpleActivityViewModelFlow.Scope::class.java

        fun openScope(parentScope: toothpick.Scope, factory: ViewModelProvider.Factory? = null, activity: FragmentActivity): toothpick.Scope {
            return parentScope.openSubScope(viewModelScopeName) {
                it.closeOnViewModelCleared(activity)
                        .installViewModelBinding(activity, factory, SimpleActivityViewModel::class.java)
            }
        }
    }

    @javax.inject.Scope
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Scope
}

fun toothpick.Scope.openSimpleActivityViewModelSubScope(activity: FragmentActivity, factory: ViewModelProvider.Factory? = null): toothpick.Scope {
    return SimpleActivityViewModelFlow.openScope(this, factory, activity)
}