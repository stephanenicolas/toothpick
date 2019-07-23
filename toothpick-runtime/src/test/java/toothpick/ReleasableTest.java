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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Qualifier;
import org.junit.After;
import org.junit.Test;
import toothpick.config.Module;
import toothpick.data.CustomScope;
import toothpick.data.Foo;
import toothpick.data.FooProvider;
import toothpick.data.FooProviderAnnotatedProvidesReleasableSingleton;
import toothpick.data.FooProviderAnnotatedReleasableSingleton;
import toothpick.data.FooReleasableSingleton;
import toothpick.data.FooReleasableSingletonInCustomScope;
import toothpick.data.IFoo;
import toothpick.data.IFooSingleton;

/*
 * Creates a instance in the simplest possible way
 * with a module that binds a single class.
 */
public class ReleasableTest {

  @After
  public void tearDown() throws Exception {
    Toothpick.reset();
  }

  @Test
  public void testReleasableSingleton_byAnnotation_shouldBeReleased() throws Exception {
    // GIVEN
    ScopeImpl scope = new ScopeImpl("");
    IFooSingleton foo = scope.getInstance(FooReleasableSingleton.class);
    IFooSingleton foo2 = scope.getInstance(FooReleasableSingleton.class);
    InternalProvider internalProvider =
        scope.mapClassesToUnNamedScopedProviders.get(FooReleasableSingleton.class);
    assertThat(internalProvider.instance, notNullValue());

    // WHEN
    scope.release();

    // THEN
    assertThat(foo, is(foo2));
    assertThat(foo, is(foo2));
    assertThat(internalProvider.instance, nullValue());
  }

  @Test
  public void testReleasableSingleton_byBinding_shouldBeReleased() throws Exception {
    // GIVEN
    ScopeImpl scope = new ScopeImpl("");
    scope.supportScopeAnnotation(CustomScope.class);
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).to(Foo.class).singleton().releasable();
          }
        });
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);
    InternalProvider internalProvider = scope.mapClassesToUnNamedScopedProviders.get(IFoo.class);
    assertThat(internalProvider.instance, notNullValue());

    // WHEN
    scope.release();

    // THEN
    assertThat(foo, is(foo2));
    assertThat(internalProvider.instance, nullValue());
  }

  @Test
  public void
      testProvidesReleasableSingleton_byAnnotation_shouldReleaseInstance_butNotProviderInstance()
          throws Exception {
    // GIVEN
    ScopeImpl scope = new ScopeImpl("");
    scope.supportScopeAnnotation(CustomScope.class);
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(FooProviderAnnotatedProvidesReleasableSingleton.class);
          }
        });
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);
    InternalProvider internalProvider = scope.mapClassesToUnNamedScopedProviders.get(IFoo.class);
    // provider instance is released when it creates a singleton
    // whether or not it is a singleton and whether or not releasable
    assertThat(internalProvider.providerInstance, notNullValue());
    assertThat(internalProvider.instance, notNullValue());

    // WHEN
    scope.release();

    // THEN
    assertThat(foo, is(foo2));
    assertThat(internalProvider.instance, nullValue());
    assertThat(internalProvider.providerInstance, notNullValue());
  }

  @Test
  public void
      testProvidesReleasableSingleton_byBinding_shouldReleaseInstance_butNotProviderInstance()
          throws Exception {
    // GIVEN
    ScopeImpl scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
          }
        });

    scope.installModules(
        new Module() {
          {
            bind(IFoo.class)
                .toProvider(FooProvider.class)
                .providesSingleton()
                .providesReleasable()
                .singleton();
          }
        });
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);
    InternalProvider internalProvider = scope.mapClassesToUnNamedScopedProviders.get(IFoo.class);
    // provider instance is released when it creates a singleton
    // whether or not it is a singleton and whether or not releasable
    assertThat(internalProvider.providerInstance, notNullValue());
    assertThat(internalProvider.instance, notNullValue());

    // WHEN
    scope.release();

    // THEN
    assertThat(foo, is(foo2));
    assertThat(internalProvider.instance, nullValue());
    assertThat(internalProvider.providerInstance, notNullValue());
  }

  @Test
  public void testProviderReleasableSingleton_byAnnotation_shouldReleaseProviderInstance()
      throws Exception {
    // GIVEN
    ScopeImpl scope = new ScopeImpl("");
    scope.supportScopeAnnotation(CustomScope.class);
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(FooProviderAnnotatedReleasableSingleton.class);
          }
        });
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);
    InternalProvider internalProvider = scope.mapClassesToUnNamedScopedProviders.get(IFoo.class);
    // provider instance is released when it creates a singleton
    // whether or not it is a singleton and whether or not releasable
    assertThat(internalProvider.providerInstance, notNullValue());
    assertThat(internalProvider.instance, nullValue());

    // WHEN
    scope.release();

    // THEN
    assertThat(foo, not(is(foo2)));
    assertThat(internalProvider.instance, nullValue());
    assertThat(internalProvider.providerInstance, nullValue());
  }

  @Test
  public void testProviderReleasableSingleton_byBinding_shouldReleaseProviderInstance()
      throws Exception {
    // GIVEN
    ScopeImpl scope = new ScopeImpl("");
    scope.supportScopeAnnotation(CustomScope.class);
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(FooProvider.class).singleton().releasable();
          }
        });
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);
    InternalProvider internalProvider = scope.mapClassesToUnNamedScopedProviders.get(IFoo.class);
    // provider instance is released when it creates a singleton
    // whether or not it is a singleton and whether or not releasable
    assertThat(internalProvider.providerInstance, notNullValue());
    assertThat(internalProvider.instance, nullValue());

    // WHEN
    scope.release();

    // THEN
    assertThat(foo, not(is(foo2)));
    assertThat(internalProvider.instance, nullValue());
    assertThat(internalProvider.providerInstance, nullValue());
  }

  @Test
  public void testReleasableSingleton_shouldBeReleased_InSubScope() throws Exception {
    // GIVEN
    ScopeImpl parentScope = (ScopeImpl) Toothpick.openScope("");
    ScopeImpl scope = (ScopeImpl) Toothpick.openScopes("", "kid");
    scope.supportScopeAnnotation(CustomScope.class);
    IFooSingleton foo = scope.getInstance(FooReleasableSingletonInCustomScope.class);
    IFooSingleton foo2 = scope.getInstance(FooReleasableSingletonInCustomScope.class);
    InternalProvider internalProvider =
        scope.mapClassesToUnNamedScopedProviders.get(FooReleasableSingletonInCustomScope.class);
    assertThat(internalProvider.instance, notNullValue());

    // WHEN
    parentScope.release();

    // THEN
    assertThat(foo, is(foo2));
    assertThat(foo, is(foo2));
    assertThat(internalProvider.instance, nullValue());
  }

  @Qualifier
  private @interface FooName {}

  private @interface NotQualifierAnnotationFooName {}
}
