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
package toothpick.inject.lazy;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import toothpick.Lazy;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.FooWithLazy;
import toothpick.data.FooWithNamedLazy;

/*
 * Test injection of {@code Lazy}s.
 */
public class InjectionOfLazyProviderTest {

  @Test
  public void testSimpleInjection() throws Exception {
    // GIVEN
    Scope scope = new ScopeImpl("");
    FooWithLazy fooWithLazy = new FooWithLazy();

    // WHEN
    Toothpick.inject(fooWithLazy, scope);

    // THEN
    assertThat(fooWithLazy.bar, notNullValue());
    assertThat(fooWithLazy.bar, isA(Lazy.class));
    Bar bar1 = fooWithLazy.bar.get();
    assertThat(bar1, isA(Bar.class));
    Bar bar2 = fooWithLazy.bar.get();
    assertThat(bar2, isA(Bar.class));
    assertThat(bar2, sameInstance(bar1));
  }

  @Test
  public void testNamedInjection() throws Exception {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(Bar.class).withName("foo").to(Bar.class);
          }
        });
    FooWithNamedLazy fooWithLazy = new FooWithNamedLazy();

    // WHEN
    Toothpick.inject(fooWithLazy, scope);

    // THEN
    assertThat(fooWithLazy.bar, notNullValue());
    assertThat(fooWithLazy.bar, isA(Lazy.class));
    Bar bar1 = fooWithLazy.bar.get();
    assertThat(bar1, isA(Bar.class));
    Bar bar2 = fooWithLazy.bar.get();
    assertThat(bar2, isA(Bar.class));
    assertThat(bar2, sameInstance(bar1));
  }

  @Test(expected = IllegalStateException.class)
  public void testLazyAfterClosingScope() throws Exception {
    // GIVEN
    String scopeName = "";
    FooWithLazy fooWithLazy = new FooWithLazy();

    // WHEN
    Toothpick.inject(fooWithLazy, Toothpick.openScope(scopeName));
    Toothpick.closeScope(scopeName);
    System.gc();

    // THEN
    fooWithLazy.bar.get(); // should crash
  }
}
