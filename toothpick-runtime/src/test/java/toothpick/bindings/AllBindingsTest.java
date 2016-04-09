package toothpick.bindings;

import javax.inject.Provider;
import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.ToothPickBaseTest;
import toothpick.config.Module;
import toothpick.data.Bar;
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
 * It also creates the injection, creation of instances, etc.
 * In this tests we also double check that all things created by toothpick via a factory
 * receive injection. All things toothpick-created are injected, that's a huge contract
 * from guice.
 * When a factory detects the things it creates, needs injection, it injects them.
 * So that when factories are used, we create injected stuff, which is larger than the contract above.
 * Larger means that a class can have a factory that inject new instances, but if we bind this class
 * to provider, the factory is not used an injection takes place only if the provides asks for it.
 * TODO this tests demonstrates that we need a strategy here again : do we want to inject stuff
 * in instances created by providers. That's not an obvious question :
 * if we do inject instances created by providers, it means that things are easy, to make them optimized we need a fast check
 * too see if injection is needed, which means finding the information wether or not the class of the things produced need injection
 * if we don't then toothpick contract is simpler : we inject stuff that toothpick create. Nothing else.
 * A configurable strategy for this would be nice.
 */
public class AllBindingsTest extends ToothPickBaseTest {

  @Test public void simpleBinding_shouldCreateInjectedInstances_whenNotSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(Foo.class);
      }
    });

    //WHEN
    Foo foo = injector.getInstance(Foo.class);
    Foo foo2 = injector.getInstance(Foo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo, not(sameInstance(foo2)));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test public void simpleBinding_shouldCreateInjectedSingletons_whenSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(FooSingleton.class);
      }
    });

    //WHEN
    FooSingleton foo = injector.getInstance(FooSingleton.class);
    FooSingleton foo2 = injector.getInstance(FooSingleton.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test public void singletonBinding_shouldCreateNonInjectedSingleton() throws Exception {
    //GIVEN
    final Foo instance = new Foo();
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(Foo.class).to(instance);
      }
    });

    //WHEN
    Foo foo = injector.getInstance(Foo.class);
    Foo foo2 = injector.getInstance(Foo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo, sameInstance(foo2));
    assertThat(foo, sameInstance(instance));
    assertThat(foo.bar, nullValue());
  }

  @Test public void bindToClass_shouldCreateInjectedInstances_whenBoundClassNotAnnotatedSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(IFoo.class).to(Foo.class);
      }
    });

    //WHEN
    IFoo foo = injector.getInstance(IFoo.class);
    IFoo foo2 = injector.getInstance(IFoo.class);

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

  @Test public void bindToClass_shouldCreateInjectedSingletons_whenBoundClassAnnotatedSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(IFooSingleton.class).to(FooSingleton.class);
      }
    });

    //WHEN
    FooSingleton foo = injector.getInstance(FooSingleton.class);
    FooSingleton foo2 = injector.getInstance(FooSingleton.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  //this test is a bit akward as we want to demonstrate that singleton providers
  //must take in charge injection by themselves. Toothpick only injects stuff in things
  //it creates.
  @Test public void bindToProviderInstance_shouldCreateNonInjectedInstances() throws Exception {
    //GIVEN
    final Provider<IFoo> providerInstance = new IFooProvider();

    Injector injector = new InjectorImpl(new Module() {
      {
        bind(IFoo.class).toProvider(providerInstance);
      }
    });

    //WHEN
    IFoo foo = injector.getInstance(IFoo.class);
    IFoo foo2 = injector.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
    assertThat(((IFooProvider) providerInstance).bar, nullValue());
  }

  @Test public void bindToProviderClass_shouldCreateNonInjectedInstances_whenProviderClassIsNotAnnotated() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(IFoo.class).toProvider(IFooProvider.class);
      }
    });

    //WHEN
    IFoo foo = injector.getInstance(IFoo.class);
    IFoo foo2 = injector.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
  }

  //we use a provider that would need to be injected and pass the injected dependence
  //to the produced object, so it's easy to test.
  @Test public void bindToProviderClass_shouldCreateInjectedProvider() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(IFoo.class).toProvider(IFooWithBarProvider.class);
      }
    });

    //WHEN
    IFoo foo = injector.getInstance(IFoo.class);
    IFoo foo2 = injector.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, notNullValue());
    assertThat(((Foo) foo2).bar, notNullValue());
  }

  @Test public void bindToProviderClass_shouldCreateNonInjectedInstancesWithProviderSingleton_whenProviderClassIsAnnotatedSingleton()
      throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(IFoo.class).toProvider(IFooProviderAnnotatedSingleton.class);
      }
    });

    //WHEN
    IFoo foo = injector.getInstance(IFoo.class);
    IFoo foo2 = injector.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
  }

  @Test public void bindToProviderClass_shouldCreateNonInjectedSingleton_whenProviderClassIsAnnotatedProvidesSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(IFoo.class).toProvider(IFooProviderAnnotatedProvidesSingleton.class);
      }
    });

    //WHEN
    IFoo foo = injector.getInstance(IFoo.class);
    IFoo foo2 = injector.getInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
  }
}
