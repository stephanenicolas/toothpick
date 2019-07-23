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
package toothpick.inject;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.Foo;
import toothpick.data.FooChildMaskingMember;
import toothpick.data.FooNested;
import toothpick.data.FooParentMaskingMember;

/*
 * Creates a instance in the simplest possible way
 * without any module.
 */
public class InjectionWithoutModuleTest {

  @Test
  public void testSimpleInjection() throws Exception {
    // GIVEN
    Scope scope = new ScopeImpl("");
    Foo foo = new Foo();

    // WHEN
    Toothpick.inject(foo, scope);

    // THEN
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void testNestedClassInjection() throws Exception {
    // GIVEN
    Scope scope = new ScopeImpl("");

    // WHEN
    FooNested fooNested = scope.getInstance(FooNested.class);
    FooNested.InnerClass1 innerClass1 = scope.getInstance(FooNested.InnerClass1.class);
    FooNested.InnerClass1.InnerClass2 innerClass2 =
        scope.getInstance(FooNested.InnerClass1.InnerClass2.class);

    // THEN
    assertThat(fooNested.bar, notNullValue());
    assertThat(fooNested.bar, isA(Bar.class));
    assertThat(innerClass1.bar, notNullValue());
    assertThat(innerClass1.bar, isA(Bar.class));
    assertThat(innerClass2.bar, notNullValue());
    assertThat(innerClass2.bar, isA(Bar.class));
  }

  @Test
  public void testInjection_shouldFail_whenFieldsAreMasked() throws Exception {
    // GIVEN
    Scope scope = new ScopeImpl("");

    // WHEN
    FooChildMaskingMember fooChildMaskingMember = scope.getInstance(FooChildMaskingMember.class);
    String parentBarToString = fooChildMaskingMember.toString();

    // THEN
    assertThat(parentBarToString, notNullValue());
    assertThat(
        fooChildMaskingMember.bar,
        not(sameInstance(((FooParentMaskingMember) fooChildMaskingMember).bar)));
  }

  @Test
  public void testInjection_shouldWork_whenInheritingBinding() throws Exception {
    // GIVEN

    Scope scope = Toothpick.openScope("root");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class).to(Foo.class);
          }
        });
    Scope childScope = Toothpick.openScopes("root", "child");
    Foo foo = new Foo();

    // WHEN
    Toothpick.inject(foo, childScope);

    // THEN
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void testInjection_shouldNotThrowAnException_whenNoDependencyIsFound() throws Exception {
    // GIVEN
    Scope scope = new ScopeImpl("root");
    NotInjectable notInjectable = new NotInjectable();

    // WHEN
    Toothpick.inject(notInjectable, scope);

    // THEN
    // nothing
  }

  class NotInjectable {}
}
