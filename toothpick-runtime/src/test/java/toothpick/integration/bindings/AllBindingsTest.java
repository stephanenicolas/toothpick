package toothpick.integration.bindings;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.Test;
import toothpick.Factory;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.MemberInjector;
import toothpick.Provider;
import toothpick.ProvidesSingleton;
import toothpick.config.Module;

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
 */
public class AllBindingsTest {

  @Test public void simpleBinding_shouldCreateInjectedInstances_whenNotSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(Foo.class);
      }
    });

    //WHEN
    Foo foo = injector.createInstance(Foo.class);
    Foo foo2 = injector.createInstance(Foo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo, not(sameInstance(foo2)));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test public void simpleBinding_shouldCreateInjectedSingletons_whenSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(FooSingleton.class);
      }
    });

    //WHEN
    FooSingleton foo = injector.createInstance(FooSingleton.class);
    FooSingleton foo2 = injector.createInstance(FooSingleton.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test public void bindToClass_shouldCreateInjectedInstances_whenBoundClassNotAnnotatedSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(IFoo.class).to(Foo.class);
      }
    });

    //WHEN
    IFoo foo = injector.createInstance(IFoo.class);
    IFoo foo2 = injector.createInstance(IFoo.class);

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
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(IFooSingleton.class).to(FooSingleton.class);
      }
    });

    //WHEN
    FooSingleton foo = injector.createInstance(FooSingleton.class);
    FooSingleton foo2 = injector.createInstance(FooSingleton.class);

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

    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(IFoo.class).toProvider(providerInstance);
      }
    });

    //WHEN
    IFoo foo = injector.createInstance(IFoo.class);
    IFoo foo2 = injector.createInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
    assertThat(((IFooProvider) providerInstance).bar, nullValue());
  }

  @Test public void bindToProviderClass_shouldCreateNonInjectedInstances_whenProviderClassIsNotAnnotated() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(IFoo.class).toProvider(IFooProvider.class);
      }
    });

    //WHEN
    IFoo foo = injector.createInstance(IFoo.class);
    IFoo foo2 = injector.createInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
    //TODO here we should be able to get the provider by injecting it
    //it requires a lot of work
    //1) change internal providers so that the instance of provider never changes
    //2) create a getProvider method in injectors
    //3) change member injector to call the get provider method appropriately
    //we will have to do something similar for Lazy anyway.
    //assertThat(((IFooProvider)providerInstance).bar, notNullValue());
  }

  @Test public void bindToProviderClass_shouldCreateNonInjectedInstancesWithProviderSingleton_whenProviderClassIsAnnotatedSingleton()
      throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(IFoo.class).toProvider(IFooProviderAnnotatedSingleton.class);
      }
    });

    //WHEN
    IFoo foo = injector.createInstance(IFoo.class);
    IFoo foo2 = injector.createInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, not(sameInstance(foo)));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
    //TODO here we should be able to get the provider by injecting it
    //it requires a lot of work
    //1) change internal providers so that the instance of provider never changes
    //2) create a getProvider method in injectors
    //3) change member injector to call the get provider method appropriately
    //we will have to do something similar for Lazy anyway.
    //assertThat(((IFooProvider)providerInstance).bar, notNullValue());
    //plus some test to check that we use the same provider
  }

  @Test public void bindToProviderClass_shouldCreateNonInjectedSingleton_whenProviderClassIsAnnotatedProvidesSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(IFoo.class).toProvider(IFooProviderAnnotatedProvidesSingleton.class);
      }
    });

    //WHEN
    IFoo foo = injector.createInstance(IFoo.class);
    IFoo foo2 = injector.createInstance(IFoo.class);

    //THEN
    assertThat(foo, notNullValue());
    assertThat(foo2, sameInstance(foo));
    assertThat(((Foo) foo).bar, nullValue());
    assertThat(((Foo) foo2).bar, nullValue());
    //TODO here we should be able to get the provider by injecting it
    //it requires a lot of work
    //1) change internal providers so that the instance of provider never changes
    //2) create a getProvider method in injectors
    //3) change member injector to call the get provider method appropriately
    //we will have to do something similar for Lazy anyway.
    //assertThat(((IFooProvider)providerInstance).bar, notNullValue());
    //plus some test to check that we use the same provider
  }

  //*******************************************************************************************
  //Test classes & the code that should be generated, duplicate those tests & use generated code.
  //TODO make external all classes below
  //*******************************************************************************************

  public static class Foo implements IFoo {
    @Inject Bar bar; //annotation is not needed, but it's a better example

    public Foo() {
    }
  }

  @Singleton //annotation is not needed, but it's a better example
  public static class FooSingleton implements IFooSingleton {
    @Inject Bar bar; //annotation is not needed, but it's a better example

    public FooSingleton() {
    }
  }

  public static class Bar {
    @Inject public Bar() {
    }
  }

  @SuppressWarnings("unused") public static class Foo$$Factory implements Factory<Foo> {
    @Override public Foo createInstance(Injector injector) {
      Foo foo = new Foo();
      injector.inject(foo);
      return foo;
    }

    @Override public boolean hasSingletonAnnotation() {
      return false;
    }

    @Override public boolean hasProducesSingletonAnnotation() {
      return false;
    }
  }

  @SuppressWarnings("unused") public static class Foo$$MemberInjector implements MemberInjector<Foo> {
    @Override public void inject(Foo foo, Injector injector) {
      foo.bar = injector.createInstance(Bar.class);
    }
  }

  @SuppressWarnings("unused") public static class FooSingleton$$MemberInjector implements MemberInjector<FooSingleton> {
    @Override public void inject(FooSingleton foo, Injector injector) {
      foo.bar = injector.createInstance(Bar.class);
    }
  }

  @SuppressWarnings("unused") public static class FooSingleton$$Factory implements Factory<FooSingleton> {
    @Override public FooSingleton createInstance(Injector injector) {
      FooSingleton foo = new FooSingleton();
      injector.inject(foo);
      return foo;
    }

    @Override public boolean hasSingletonAnnotation() {
      return true;
    }

    @Override public boolean hasProducesSingletonAnnotation() {
      return false;
    }
  }

  @SuppressWarnings("unused") public static class Bar$$Factory implements Factory<Bar> {
    @Override public Bar createInstance(Injector injector) {
      return new Bar();
    }

    @Override public boolean hasSingletonAnnotation() {
      return false;
    }

    @Override public boolean hasProducesSingletonAnnotation() {
      return false;
    }
  }

  private interface IFooSingleton {
  }

  private interface IFoo {
  }

  private static class IFooProvider implements Provider<IFoo> {
    @Inject Bar bar;

    @Inject public IFooProvider() {
    }

    @Override public IFoo get() {
      return new Foo();
    }
  }

  @Singleton private static class IFooProviderAnnotatedSingleton implements Provider<IFoo> {
    @Inject Bar bar;

    @Inject public IFooProviderAnnotatedSingleton() {
    }

    @Override public IFoo get() {
      return new Foo();
    }
  }

  @ProvidesSingleton private static class IFooProviderAnnotatedProvidesSingleton implements Provider<IFoo> {
    @Inject Bar bar;

    @Inject public IFooProviderAnnotatedProvidesSingleton() {
    }

    @Override public IFoo get() {
      return new Foo();
    }
  }

  @SuppressWarnings("unused") public static class IFooProvider$$Factory implements Factory<IFooProvider> {
    @Override public IFooProvider createInstance(Injector injector) {
      IFooProvider iFooProvider = new IFooProvider();
      injector.inject(iFooProvider);
      return iFooProvider;
    }

    @Override public boolean hasSingletonAnnotation() {
      return false;
    }

    @Override public boolean hasProducesSingletonAnnotation() {
      return false;
    }
  }

  @SuppressWarnings("unused") public static class IFooProviderAnnotatedSingleton$$Factory implements Factory<IFooProviderAnnotatedSingleton> {
    @Override public IFooProviderAnnotatedSingleton createInstance(Injector injector) {
      IFooProviderAnnotatedSingleton iFooProviderAnnotatedSingleton = new IFooProviderAnnotatedSingleton();
      injector.inject(iFooProviderAnnotatedSingleton);
      return iFooProviderAnnotatedSingleton;
    }

    @Override public boolean hasSingletonAnnotation() {
      return true;
    }

    @Override public boolean hasProducesSingletonAnnotation() {
      return false;
    }
  }

  @SuppressWarnings("unused") public static class IFooProviderAnnotatedProvidesSingleton$$Factory
      implements Factory<IFooProviderAnnotatedProvidesSingleton> {
    @Override public IFooProviderAnnotatedProvidesSingleton createInstance(Injector injector) {
      IFooProviderAnnotatedProvidesSingleton iFooProviderAnnotatedSingleton = new IFooProviderAnnotatedProvidesSingleton();
      injector.inject(iFooProviderAnnotatedSingleton);
      return iFooProviderAnnotatedSingleton;
    }

    @Override public boolean hasSingletonAnnotation() {
      return false;
    }

    @Override public boolean hasProducesSingletonAnnotation() {
      return true;
    }
  }

  @SuppressWarnings("unused") public static class IFooProvider$$MemberInjector implements MemberInjector<IFooProvider> {
    @Override public void inject(IFooProvider foo, Injector injector) {
      foo.bar = injector.createInstance(Bar.class);
    }
  }

  @SuppressWarnings("unused") public static class IFooProviderAnnotatedSingleton$$MemberInjector
      implements MemberInjector<IFooProviderAnnotatedSingleton> {
    @Override public void inject(IFooProviderAnnotatedSingleton foo, Injector injector) {
      foo.bar = injector.createInstance(Bar.class);
    }
  }

  @SuppressWarnings("unused") public static class IFooProviderAnnotatedProvidesSingleton$$MemberInjector
      implements MemberInjector<IFooProviderAnnotatedProvidesSingleton> {
    @Override public void inject(IFooProviderAnnotatedProvidesSingleton foo, Injector injector) {
      foo.bar = injector.createInstance(Bar.class);
    }
  }
}
