package toothpick.smoothie.viewmodel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import toothpick.Scope
import toothpick.config.Module

fun Scope.closeOnViewModelCleared(activity: FragmentActivity): Scope {
    ViewModelUtil.closeOnViewModelCleared(activity, this)
    return this
}

fun Scope.closeOnViewModelCleared(fragment: Fragment): Scope {
    ViewModelUtil.closeOnViewModelCleared(fragment, this)
    return this
}

fun <T> Scope.installViewModelBinding(activity: FragmentActivity, factory: Factory? = null, viewModelClass: Class<T>) : Scope where T: ViewModel {
    installModules(object : Module() {
        init {
            bind(viewModelClass).toProviderInstance(ViewModelProvider(activity, factory, viewModelClass))
        }
    })

    return this
}

fun <T> Scope.installViewModelBinding(fragment: Fragment, factory: Factory?  = null, viewModelClass: Class<T>) : Scope where T: ViewModel {
    installModules(object : Module() {
        init {
            bind(viewModelClass).toProviderInstance(ViewModelProvider(fragment, factory, viewModelClass))
        }
    })

    return this
}
