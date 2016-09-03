package toothpick;

import org.junit.Test;

import javax.inject.Provider;

import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.data.Bar;
import toothpick.data.CustomScope;
import toothpick.data.Foo;
import toothpick.data.FooSingleton;
import toothpick.data.IFoo;
import toothpick.data.IFooProvider;
import toothpick.data.IFooProviderAnnotatedProvidesSingleton;
import toothpick.data.IFooProviderAnnotatedSingleton;
import toothpick.data.IFooSingleton;
import toothpick.data.IFooWithBarProvider;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test all possible ways to bind stuff in modules.
 * We also tests the injection, creation of instances, etc.
 * In these tests we also double check that all things created by toothpick via a factory
 * receive injection. All things toothpick-created are injected, that's a huge contract
 * from guice that toothpick honors as well.
 * All things created by toothpick are injected.
 */
public class AllBindingsTest extends ToothpickBaseTest {

  @Test
  public void simpleBinding_shouldCreateInjectedInstances_whenNotSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(Foo.class);
      }
    });

    //WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo, not(sameInstance(foo2)));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void simpleBinding_shouldCreateInjectedSingletons_whenSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(FooSingleton.class);
      }
    });

    //WHEN
    FooSingleton foo = scope.getInstance(FooSingleton.class);
    FooSingleton foo2 = scope.getInstance(FooSingleton.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void singletonBinding_shouldCreateNonInjectedSingleton() throws Exception {
    //GIVEN
    final Foo instance = new Foo();
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(Foo.class).toInstance(instance);
      }
    });

    //WHEN
    Foo foo = scope.getInstance(Foo.class);
    Foo foo2 = scope.getInstance(Foo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo, sameInstance(foo2));
    assertThat(foo, sameInstance(instance));
    assertThat(foo.bar, nullValue());
  }

  @Test
  public void bindToClass_shouldCreateInjectedInstances_whenBoundClassNotAnnotatedSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(IFoo.class).to(Foo.class);
      }
    });

    //WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    //THEN
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
  public void bindToClass_shouldCreateInjectedSingletons_whenBoundClassAnnotatedSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(IFooSingleton.class).to(FooSingleton.class);
      }
    });

    //WHEN
    FooSingleton foo = scope.getInstance(FooSingleton.class);
    FooSingleton foo2 = scope.getInstance(FooSingleton.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void bindToClass_shouldCreateInjectedSingletons_whenBoundClassAnnotatedSingletonAndRuntimeCheckOn() throws Exception {
    //GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment());
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(FooSingleton.class);
      }
    });

    //WHEN
    FooSingleton foo = scope.getInstance(FooSingleton.class);
    FooSingleton foo2 = scope.getInstance(FooSingleton.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  //this test is a bit akward as we want to demonstrate that singleton providers
  //must take in charge injection by themselves. Toothpick only injects stuff in things
  //it creates.
  @Test
  public void bindToProviderInstance_shouldCreateNonInjectedInstances() throws Exception {
    //GIVEN
    final Provider<IFoo> providerInstance = new IFooProvider();

    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(IFoo.class).toProviderInstance(providerInstance);
      }
    });

    //WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
    assertThat(((IFooProvider) providerInstance).bar, nullValue());
  }

  @Test
  public void bindToProviderClass_shouldCreateNonInjectedInstances_whenProviderClassIsNotAnnotated() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(IFoo.class).toProvider(IFooProvider.class);
      }
    });

    //WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
  }

  //we use a provider that would need to be injected and pass the injected dependence
  //to the produced object, so it's easy to test.
  @Test
  public void bindToProviderClass_shouldCreateInjectedProvider() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(IFoo.class).toProvider(IFooWithBarProvider.class);
      }
    });

    //WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, notNullValue());
    assertThat(((Foo) foo2).bar, notNullValue());
  }

  @Test
  public void bindToProviderClass_shouldCreateNonInjectedInstancesWithProviderSingleton_whenProviderClassIsAnnotatedSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(IFoo.class).toProvider(IFooProviderAnnotatedSingleton.class);
      }
    });

    //WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
  }

  @Test
  public void bindToProviderClass_shouldCreateNonInjectedSingleton_whenProviderClassIsAnnotatedProvidesSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.bindScopeAnnotation(CustomScope.class);
    scope.installModules(new Module() {
      {
        bind(IFoo.class).toProvider(IFooProviderAnnotatedProvidesSingleton.class);
      }
    });

    //WHEN
    IFoo foo = scope.getInstance(IFoo.class);
    IFoo foo2 = scope.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
  }
}
