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

/**
 * Strategy pattern that allows to change various behaviors of Toothpick. The default configuration
 * is {@link #forProduction()}. A custom configuration can be created and used by toothpick, it is
 * even possible to use a composition of the built-in configurations.
 */
public class Configuration
    implements RuntimeCheckConfiguration, MultipleRootScopeCheckConfiguration {

  private RuntimeCheckConfiguration runtimeCheckConfiguration = new RuntimeCheckOffConfiguration();
  private MultipleRootScopeCheckConfiguration multipleRootScopeCheckConfiguration =
      new MultipleRootScopeCheckOffConfiguration();

  /**
   * Performs many runtime checks. This configuration reduces performance. It should be used only
   * during development. The checks performed are:
   *
   * <ul>
   *   <li>cycle detection: check that not 2 classes depend on each other. Note that if of them uses
   *       a Lazy instance of the other or a Producer, then there is no such cycle.
   *   <li>illegal binding detection: check no scope annotated class is used as the target of a
   *       binding.
   * </ul>
   *
   * @return a development configuration.
   */
  public static Configuration forDevelopment() {
    final Configuration configuration = new Configuration();
    configuration.runtimeCheckConfiguration = new RuntimeCheckOnConfiguration();
    return configuration;
  }

  /**
   * Performs no runtime checks. This configuration is faster than {@link #forDevelopment()}. It can
   * be used in production.
   *
   * @return a production configuration.
   */
  public static Configuration forProduction() {
    return new Configuration();
  }

  /**
   * Allows multiple root scopes in the scope forest.
   *
   * @return a configuration that allows multiple root scopes.
   */
  public Configuration allowMultipleRootScopes() {
    this.multipleRootScopeCheckConfiguration = new MultipleRootScopeCheckOffConfiguration();
    return this;
  }

  /**
   * Prevents the creation of multiple root scopes in the scope forest. TP scope forest will be
   * restricted to a scope tree. On android this option can help to detect when a scope is reopened
   * after it was destroyed.
   *
   * @return a configuration that allows a single root scope.
   */
  public Configuration preventMultipleRootScopes() {
    this.multipleRootScopeCheckConfiguration = new MultipleRootScopeCheckOnConfiguration();
    return this;
  }

  @Override
  public void checkIllegalBinding(Binding binding, Scope scope) {
    runtimeCheckConfiguration.checkIllegalBinding(binding, scope);
  }

  @Override
  public void checkCyclesStart(Class clazz, String name) {
    runtimeCheckConfiguration.checkCyclesStart(clazz, name);
  }

  @Override
  public void checkCyclesEnd(Class clazz, String name) {
    runtimeCheckConfiguration.checkCyclesEnd(clazz, name);
  }

  @Override
  public void checkMultipleRootScopes(Scope scope) {
    multipleRootScopeCheckConfiguration.checkMultipleRootScopes(scope);
  }

  @Override
  public void onScopeForestReset() {
    multipleRootScopeCheckConfiguration.onScopeForestReset();
  }
}
