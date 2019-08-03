/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.smoothie.viewmodel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import toothpick.Scope

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
inline fun <reified T : ViewModel> Scope.installViewModelBinding(activity: FragmentActivity, factory: Factory? = null): Scope {
    ViewModelUtil.installViewModelBinding(this, activity, T::class.java, factory)
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
inline fun <reified T : ViewModel> Scope.installViewModelBinding(fragment: Fragment, factory: Factory? = null): Scope {
    ViewModelUtil.installViewModelBinding(this, fragment, T::class.java, factory)
    return this
}
