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
package toothpick.smoothie.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider.Factory;
import androidx.lifecycle.ViewModelProviders;
import javax.inject.Provider;
import toothpick.Scope;

/**
 * Can be used in a binding to inject the view model. Beware of installing such a binding in a scope
 * that doesn't depend on the activity life cycle, it would be ineffective. If one wants to rewrite
 * this class, make sure that the provider doesn't leak the activity.
 *
 * @param <T> a view model class. Warning this provider is not scoped. Ideally, it should be a
 *     singleton of a view model scope.
 */
public class ViewModelProvider<T extends ViewModel> implements Provider<T> {
  private final T viewModel;
  private Scope scope;

  /**
   * Provides instances of a {@link ViewModel} class. Internally this provider will use {@link
   * ViewModelProviders#of(FragmentActivity)} to obtain the instance. This is required so that the
   * view model is registered by the view model extension, and its onCleared method is called.
   *
   * @param scope the scope used to install the binding.
   * @param activity holds the view model.
   * @param viewModelClass the class of the view model instance to return.
   */
  public ViewModelProvider(
      @NonNull Scope scope,
      @NonNull FragmentActivity activity,
      @NonNull Class<? extends T> viewModelClass) {
    // we should not keep the activity, otherwise, it will leak from the view model scope
    viewModel = ViewModelProviders.of(activity).get(viewModelClass);
    this.scope = scope;
  }

  /**
   * Provides instances of a {@link ViewModel} class. Internally this provider will use {@link
   * ViewModelProviders#of(FragmentActivity)} to obtain the instance. This is required so that the
   * view model is registered by the view model extension, and its onCleared method is called.
   *
   * @param scope the scope used to install the binding.
   * @param activity holds the view model.
   * @param factory required to build view model instances.
   * @param viewModelClass the class of the view model instance to return.
   */
  public ViewModelProvider(
      @NonNull Scope scope,
      @NonNull FragmentActivity activity,
      @Nullable Factory factory,
      @NonNull Class<? extends T> viewModelClass) {
    // we should not keep the activity, otherwise, it will leak from the view model scope
    viewModel = ViewModelProviders.of(activity, factory).get(viewModelClass);
    this.scope = scope;
  }

  /**
   * Provides instances of a {@link ViewModel} class. Internally this provider will use {@link
   * ViewModelProviders#of(Fragment)} to obtain the instance. This is required so that the view
   * model is registered by the view model extension, and its onCleared method is called.
   *
   * @param scope the scope used to install the binding.
   * @param fragment holds the view model.
   * @param viewModelClass the class of the view model instance to return.
   */
  public ViewModelProvider(
      @NonNull Scope scope,
      @NonNull Fragment fragment,
      @NonNull Class<? extends T> viewModelClass) {
    // we should not keep the activity, otherwise, it will leak from the view model scope
    viewModel = ViewModelProviders.of(fragment).get(viewModelClass);
    this.scope = scope;
  }

  /**
   * Provides instances of a {@link ViewModel} class. Internally this provider will use {@link
   * ViewModelProviders#of(Fragment)} to obtain the instance. This is required so that the view
   * model is registered by the view model extension, and its onCleared method is called.
   *
   * @param scope the scope used to install the binding.
   * @param fragment holds the view model.
   * @param factory required to build view model instances.
   * @param viewModelClass the class of the view model instance to return.
   */
  public ViewModelProvider(
      @NonNull Scope scope,
      @NonNull Fragment fragment,
      @Nullable Factory factory,
      @NonNull Class<? extends T> viewModelClass) {
    // we should not keep the activity, otherwise, it will leak from the view model scope
    viewModel = ViewModelProviders.of(fragment, factory).get(viewModelClass);
    this.scope = scope;
  }

  /**
   * Wraps a view model instance for injection.
   *
   * @param scope the scope used to install the binding.
   * @param viewModel the view model instance that will always be injected.
   */
  public ViewModelProvider(@NonNull Scope scope, @NonNull T viewModel) {
    // we should not keep the activity, otherwise, it will leak.
    this.viewModel = viewModel;
    this.scope = scope;
  }

  @Override
  public T get() {
    if (scope != null) {
      scope.inject(viewModel);
      scope = null; // No need to keep the scope longer
    }
    return viewModel;
  }
}
