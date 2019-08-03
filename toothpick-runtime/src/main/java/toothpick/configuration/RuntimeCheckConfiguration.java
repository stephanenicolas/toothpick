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
import toothpick.config.Binding;

/** Defines a check strategy. */
interface RuntimeCheckConfiguration {
  /**
   * Checks that a binding is legal in the current scope.
   *
   * @param binding the binding being installed.
   * @param scope the scope where the binding is installed.
   */
  void checkIllegalBinding(Binding binding, Scope scope);

  /**
   * Called when the class {@code class} starts being injected using the qualifier {@code name}.
   * Will check whether or not there is a cycle in the dependencies of this injection (i.e. a
   * dependency transitively needs itself).
   *
   * @param clazz the class to be injected.
   * @param name the name of the required injection.
   */
  void checkCyclesStart(Class clazz, String name);

  /**
   * Called when the class {@code class} ends being injected using the qualifier {@code name}. Will
   * check whether or not there is a cycle in the dependencies of this injection (i.e. a dependency
   * transitively needs itself).
   *
   * @param clazz the class to be injected.
   * @param name the name of the required injection.
   */
  void checkCyclesEnd(Class clazz, String name);
}
