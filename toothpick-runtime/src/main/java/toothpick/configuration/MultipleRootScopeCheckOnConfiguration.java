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
package toothpick.configuration;

import toothpick.Scope;

/**
 * Checks that TP scope forest is actually a tree. If a new created scope is a new root outside of
 * the first created tree, it will throw a {@link MultipleRootException}.
 */
class MultipleRootScopeCheckOnConfiguration implements MultipleRootScopeCheckConfiguration {
  private Scope rootScope;

  @Override
  public synchronized void checkMultipleRootScopes(Scope newRootScope) {
    if (rootScope == null && newRootScope != null) {
      rootScope = newRootScope;
      return;
    }

    if (newRootScope == rootScope) {
      return;
    }

    throw new MultipleRootException(newRootScope);
  }

  @Override
  public synchronized void onScopeForestReset() {
    rootScope = null;
  }
}
