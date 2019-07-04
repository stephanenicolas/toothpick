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
package toothpick.testing;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import toothpick.Scope;
import toothpick.Toothpick;

public class ToothPickExtension implements AfterEachCallback {

  private final ToothPickTestModule testModule;
  private Scope scope;

  public ToothPickExtension(Object test) {
    this(test, null);
  }

  public ToothPickExtension(Object test, Object scopeName) {
    this.testModule = new ToothPickTestModule(test);
    if (scopeName != null) {
      setScopeName(scopeName);
    }
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    Toothpick.reset();
  }

  public void setScopeName(Object scopeName) {
    if (scope != null) {
      throw new IllegalStateException(
          "scope is already initialized, use a constructor without a scope name for the rule.");
    }
    scope = Toothpick.openScope(scopeName);
    scope.installTestModules(testModule);
  }

  public ToothPickTestModule getTestModule() {
    return testModule;
  }

  public Scope getScope() {
    return scope;
  }

  public void inject(Object objectUnderTest) {
    Toothpick.inject(objectUnderTest, scope);
  }

  public <T> T getInstance(Class<T> clazz) {
    return getInstance(clazz, null);
  }

  public <T> T getInstance(Class<T> clazz, String name) {
    return scope.getInstance(clazz, name);
  }
}
