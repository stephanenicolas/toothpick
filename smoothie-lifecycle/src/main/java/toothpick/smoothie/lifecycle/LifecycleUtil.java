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
package toothpick.smoothie.lifecycle;

import static toothpick.Toothpick.closeScope;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import toothpick.Scope;

/**
 * Provides support for Android architecture components. Closes scopes automatically following a
 * life cycle owner's onDestroy event.
 */
public class LifecycleUtil {

  private LifecycleUtil() {
    throw new RuntimeException("Should not be instantiated");
  }

  /**
   * The {@code scope} will be closed automatically during {@code owner}'s {@code onDestroy} event.
   *
   * @param owner the lifecycle owner to observe.
   * @param scope the scope to be closed automatically during {@code owner}'s {@code onDestroy}
   *     event.
   */
  public static void closeOnDestroy(
      final @NonNull LifecycleOwner owner, final @NonNull Scope scope) {
    owner
        .getLifecycle()
        .addObserver(
            new DefaultLifecycleObserver() {
              @Override
              public void onDestroy(@NonNull LifecycleOwner owner) {
                closeScope(scope.getName());
              }
            });
  }
}
