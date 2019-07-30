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

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Provider;
import org.junit.Test;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.CustomScope;
import toothpick.data.Foo;
import toothpick.data.FooProvider;
import toothpick.data.FooProviderAnnotatedProvidesSingleton;
import toothpick.data.FooProviderAnnotatedSingleton;
import toothpick.data.FooProviderReusingInstance;
import toothpick.data.FooSingleton;
import toothpick.data.IFoo;
import toothpick.data.IFooProvider;
import toothpick.data.IFooSingleton;
import toothpick.data.IFooWithBarProvider;

/**
 * Test all possible ways to bind stuff in modules. We also tests the injection, creation of
 * instances, etc. In these tests we also double check that all things created by toothpick via a
 * factory receive injection. All things toothpick-created are injected, that's a huge contract from
 * guice that toothpick honors as well. All things created by toothpick are injected.
 */
public class AllBindingsTestWithDefaultConfiguration {

  @Test
  public void simpleBinding_shouldCreateInjectedInstances_whenNotSingleton() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class);
          }
        });

    // WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo, not(sameInstance(foo2)));
    assertThat(foo.bar, notNullValue());
  }

  @Test
  public void simpleBinding_shouldCreateInjectedInstances_whenCreateinScopeViaCode() {
    // GIVEN
    Scope scope = new ScopeImpl("root");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class);
          }
        });

    // WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo, not(sameInstance(foo2)));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void simpleBinding_shouldCreateInjectedSingletons_whenSingletonViaAnnotation() {
    // GIVEN
    Scope scope = new ScopeImpl("root");
    scope.installModules(
        new Module() {
          {
            bind(FooSingleton.class);
          }
        });

    // WHEN
    FooSingleton foo = scope.getInstance(FooSingleton.class);
    FooSingleton foo2 = scope.getInstance(FooSingleton.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void simpleBinding_shouldCreateInjectedSingletons_whenSingletonViaCode() {
    // GIVEN
    Scope scope = new ScopeImpl("root");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class).singleton();
          }
        });

    // WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void providerClassBinding_shouldProvideSingletons_whenProvidesSingletonViaCode() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class).toProvider(FooProvider.class).providesSingleton();
          }
        });

    // WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
  }

  @Test
  public void providerClassBinding_shouldCreateProviderSingleton_whenSingletonViaCode() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class).toProvider(FooProviderReusingInstance.class).singleton();
          }
        });

    // WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
  }

  @Test
  public void providerClassBinding_shouldCreateInstancesViaProviderInstances_whenInScopeViaCode() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class).toProvider(FooProviderReusingInstance.class);
          }
        });

    // WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
  }

  @Test
  public void
      providerClassBinding_shouldCreateInstancesViaProviderInstances_whenProviderClassIsNotAnnotated() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(IFooProvider.class);
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
    assertThat(((Foo) foo).bar, not(sameInstance(((Foo) foo2).bar)));
  }

  // we use a provider that would need to be injected and pass the injected dependence
  // to the produced object, so it's easy to test.
  @Test
  public void providerClassBinding_shouldCreateInjectedProviderInstances() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(IFooWithBarProvider.class);
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
    assertThat(((Foo) foo).bar, not(sameInstance(((Foo) foo2).bar)));
  }

  @Test
  public void
      providerClassBinding_shouldCreateNonInjectedInstancesWithProviderSingleton_whenProviderClassIsAnnotatedSingleton() {
    // GIVEN
    Scope scope = new ScopeImpl("");
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

  @Test
  public void
      providerClassBinding_shouldCreateNonInjectedSingleton_whenProviderClassIsAnnotatedProvidesSingleton() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.supportScopeAnnotation(CustomScope.class);
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(FooProviderAnnotatedProvidesSingleton.class);
          }
        });

    // WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
  }

  @Test
  public void providerInstanceBinding_shouldProvideSingletons_whenProvidesSingletonViaCode() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class).toProviderInstance(new FooProvider()).providesSingleton();
          }
        });

    // WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
  }

  // this test is a bit akward as we want to demonstrate that singleton providers
  // must take in charge injection by themselves. Toothpick only injects stuff in things
  // it creates.
  @Test
  public void providerInstanceBinding_shouldCreateNonInjectedInstances() {
    // GIVEN
    final Provider<IFoo> providerInstance = new IFooProvider();

    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).toProviderInstance(providerInstance);
          }
        });

    // WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
    assertThat(((IFooProvider) providerInstance).bar, nullValue());
  }

  @Test
  public void singletonBinding_shouldCreateNonInjectedSingleton() {
    // GIVEN
    final Foo instance = new Foo();
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class).toInstance(instance);
          }
        });

    // WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo, sameInstance(foo2));
    assertThat(foo, sameInstance(instance));
    assertThat(foo.bar, nullValue());
  }

  @Test
  public void classBinding_shouldCreateInjectedInstances_whenBoundClassNotAnnotatedSingleton() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).to(Foo.class);
          }
        });

    // WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo, isA(IFoo.class));
    assertThat(foo2, isA(IFoo.class));
    assertThat(foo, not(sameInstance(foo2)));
    assertThat(((Foo) foo).bar, notNullValue());
    assertThat(((Foo) foo).bar, isA(Bar.class));
    assertThat(((Foo) foo2).bar, notNullValue());
    assertThat(((Foo) foo2).bar, isA(Bar.class));
  }

  @Test
  public void classBinding_shouldCreateInjectedSingletons_whenBoundClassAnnotatedSingleton() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(IFooSingleton.class).to(FooSingleton.class);
          }
        });

    // WHEN
    FooSingleton foo = scope.getInstance(FooSingleton.class);
    FooSingleton foo2 = scope.getInstance(FooSingleton.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void classBinding_shouldCreateInjectedSingletons_whenSingletonViaCode() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).to(Foo.class).singleton();
          }
        });

    // WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(((Foo) foo).bar, isA(Bar.class));
  }

  @Test
  public void classBinding_shouldCreateInstances_wheninScopeViaCode() {
    // GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(IFoo.class).to(Foo.class);
          }
        });

    // WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    // THEN
    assertThat(foo, notNullValue());
    assertThat(foo, isA(IFoo.class));
    assertThat(foo2, isA(IFoo.class));
    assertThat(foo, not(sameInstance(foo2)));
    assertThat(((Foo) foo).bar, notNullValue());
    assertThat(((Foo) foo).bar, isA(Bar.class));
    assertThat(((Foo) foo2).bar, notNullValue());
    assertThat(((Foo) foo2).bar, isA(Bar.class));
  }
}
