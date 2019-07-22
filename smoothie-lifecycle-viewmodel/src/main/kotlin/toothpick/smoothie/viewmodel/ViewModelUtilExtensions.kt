package toothpick.smoothie.viewmodel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import toothpick.Scope
import toothpick.config.Module

/**
 * Closes a scope automatically when the view model of a {@link FragmentActivity} is cleared.
 *
 * @param activity the fragment activity to observe.
 */
fun Scope.closeOnViewModelCleared(activity: FragmentActivity): Scope {
    ViewModelUtil.closeOnViewModelCleared(activity, this)
    return this
}

/**
 * Closes a scope automatically when the view model of a {@link Fragment} is cleared.
 *
 * @param fragment the fragment to observe.
 */
fun Scope.closeOnViewModelCleared(fragment: Fragment): Scope {
    ViewModelUtil.closeOnViewModelCleared(fragment, this)
    return this
}

/**
 * Installs a binding for a view model class.
 * It will become available by injection. Beware that such a binding
 * should be installed on a scope that is independent of the activity life cycle.
 *
 * @param activity the fragment activity to observe.
 * @param factory optional factory needed to create the view model instances.
 * @param viewModelClass the class of the view model to inject.
 */
fun <T> Scope.installViewModelBinding(activity: FragmentActivity, factory: Factory? = null, viewModelClass: Class<T>) : Scope where T: ViewModel {
    installModules(object : Module() {
        init {
            bind(viewModelClass).toProviderInstance(ViewModelProvider(activity, factory, viewModelClass))
        }
    })

    return this
}

/**
 * Installs a binding for a view model class.
 * It will become available by injection. Beware that such a binding
 * should be installed on a scope that is independent of the fragment life cycle.
 *
 * @param fragment the fragment to observe.
 * @param factory optional factory needed to create the view model instances.
 * @param viewModelClass the class of the view model to inject.
 */
fun <T> Scope.installViewModelBinding(fragment: Fragment, factory: Factory?  = null, viewModelClass: Class<T>) : Scope where T: ViewModel {
    installModules(object : Module() {
        init {
            bind(viewModelClass).toProviderInstance(ViewModelProvider(fragment, factory, viewModelClass))
        }
    })

    return this
}
