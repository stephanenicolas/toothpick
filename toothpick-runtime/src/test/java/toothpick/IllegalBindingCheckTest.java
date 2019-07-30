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
package toothpick;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.data.Foo;
import toothpick.data.FooProviderAnnotatedSingleton;
import toothpick.data.IFoo;

public class IllegalBindingCheckTest {
  @Test(expected = RuntimeException.class)
  public void
      providerClassBinding_shouldFailToInstallBinding_whenAnnotationScopeIsNotMatchedByInstallationScope_InDevConfig() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment());
    Scope scope = Toothpick.openScopes("", "child");

    // WHEN
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(FooProviderAnnotatedSingleton.class);
          }
        });

    // THEN
    fail("Test should have thrown an exception.");
  }

  @Test
  public void
      providerClassBinding_shouldFailToInstallBinding_whenAnnotationScopeIsNotMatchedByInstallationScope_InProdConfig() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forProduction());
    Scope scope = Toothpick.openScopes("", "child");
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(FooProviderAnnotatedSingleton.class);
          }
        });

    // WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, notNullValue());
    assertThat(((Foo) foo2).bar, notNullValue());
    assertThat(((Foo) foo).bar, sameInstance(((Foo) foo2).bar));
  }
}
