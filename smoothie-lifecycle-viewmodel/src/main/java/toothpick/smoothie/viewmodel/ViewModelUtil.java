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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import toothpick.Scope;

import static toothpick.Toothpick.closeScope;

/**
 * Provides support for Android architecture components's view model. Closes scopes automatically
 * following a view model's onClear event.
 */
public class ViewModelUtil {

  private ViewModelUtil() {
    throw new RuntimeException("Should not be instantiated");
  }

  /**
   * Close a scope automatically when the view model of a {@link FragmentActivity} is cleared.
   *
   * @param activity the fragment activity to observe.
   * @param scope the scope that will be closed when the view model of a {@link FragmentActivity} is
   *     cleared.
   */
  public static void closeOnViewModelCleared(@NonNull FragmentActivity activity, @NonNull Scope scope) {
    ViewModelProvider.Factory factory = new TPViewModelFactory(scope);
    ViewModelProviders.of(activity, factory).get(TPViewModel.class);
  }

  /**
   * Close a scope automatically when the view model of a {@link Fragment} is cleared.
   *
   * @param fragment the fragment activity to observe.
   * @param scope the scope that will be closed when the view model of a {@link FragmentActivity} is
   *     cleared.
   */
  public static void closeOnViewModelCleared(@NonNull Fragment fragment, @NonNull Scope scope) {
    ViewModelProvider.Factory factory = new TPViewModelFactory(scope);
    ViewModelProviders.of(fragment, factory).get(TPViewModel.class);
  }

  private static class TPViewModelFactory implements ViewModelProvider.Factory {
    private Scope scope;

    public TPViewModelFactory(Scope scope) {
      this.scope = scope;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new TPViewModel(scope);
    }
  }

  /**
   * Internal view model that closes a scope when {@link #onCleared()} is invoked.
   */
  private static class TPViewModel extends ViewModel {
    private Scope scope;

    public TPViewModel(Scope scope) {

      this.scope = scope;
    }

    @Override
    protected void onCleared() {
      super.onCleared();
      closeScope(scope.getName());
    }
  }
}
