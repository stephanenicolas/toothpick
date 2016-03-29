package toothpick.integration.getInstance.inject;

import javax.inject.Inject;
import org.junit.Test;
import toothpick.Factory;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.MemberInjector;
import toothpick.Provider;
import toothpick.config.Module;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class CreatedInstancesAreInjectedWhenNeeded {

  @Test public void createdInstance_shouldBeInjected_whenBindingToAClassWithInjectFields() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(Foo.class).to(Foo.class);
      }
    });

    //WHEN
    Foo foo = injector.createInstance(Foo.class);

    //THEN
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test public void createdInstance_shouldNotBeInjected_whenBindingToAProvider() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(Foo.class).toProvider(new Provider<Foo>() {
          @Override public Foo get() {
            return new Foo();
          }
        });
      }
    });

    //WHEN
    Foo foo = injector.createInstance(Foo.class);

    //THEN
    assertThat(foo.bar, nullValue());
  }

  @Test public void createdInstance_shouldNotBeInjected_whenBindingToAProviderClass() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(Foo.class).toProvider(FooProvider.class);
      }
    });

    //WHEN
    Foo foo = injector.createInstance(Foo.class);

    //THEN
    assertThat(foo.bar, nullValue());
  }

  @Test public void createdProvider_shouldBeInjected_whenBindingToAProviderClassThatHasInjectedFields() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, "foo", new Module() {
      {
        bind(Foo.class).toProvider(FooProvider.class);
      }
    });

    //WHEN
    Foo foo = injector.createInstance(Foo.class);

    //THEN
    assertThat(foo.bar, nullValue());
  }

  public static class FooProvider implements Provider<Foo> {
    @Inject Bar bar; //annotation is not needed, but it's a better example

    @Override public Foo get() {
      return new Foo();
    }
  }

  @SuppressWarnings("unused") public static class FooProvider$$MemberInjector implements MemberInjector<FooProvider> {
    @Override public void inject(FooProvider foo, Injector injector) {
      Bar bar = injector.createInstance(Bar.class);
      foo.bar = bar;
    }
  }

  public static class Foo {
    @Inject Bar bar; //annotation is not needed, but it's a better example

    public Foo() {
    }
  }

  public static class Bar {
    @Inject public Bar() {
    }
  }

  @SuppressWarnings("unused") public static class FooProvider$$Factory implements Factory<FooProvider> {
    @Inject Bar bar; //annotation is not needed, but it's a better example

    @Override public FooProvider createInstance(Injector injector) {
      FooProvider fooProvider = new FooProvider();
      injector.inject(fooProvider);
      return fooProvider;
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
}
