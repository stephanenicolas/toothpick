package toothpick.integration.getInstance.inject;

import javax.inject.Provider;
import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.config.Module;
import toothpick.integration.data.Bar;
import toothpick.integration.data.Foo;
import toothpick.integration.data.FooProvider;

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
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(Foo.class).to(Foo.class);
      }
    });

    //WHEN
    Foo foo = injector.getInstance(Foo.class);

    //THEN
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test public void createdInstance_shouldNotBeInjected_whenBindingToAProvider() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(Foo.class).toProvider(new Provider<Foo>() {
          @Override public Foo get() {
            return new Foo();
          }
        });
      }
    });

    //WHEN
    Foo foo = injector.getInstance(Foo.class);

    //THEN
    assertThat(foo.bar, nullValue());
  }

  @Test public void createdInstance_shouldNotBeInjected_whenBindingToAProviderClass() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(Foo.class).toProvider(FooProvider.class);
      }
    });

    //WHEN
    Foo foo = injector.getInstance(Foo.class);

    //THEN
    assertThat(foo.bar, nullValue());
  }

  @Test public void createdProvider_shouldBeInjected_whenBindingToAProviderClassThatHasInjectedFields() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new Module() {
      {
        bind(Foo.class).toProvider(FooProvider.class);
      }
    });

    //WHEN
    Foo foo = injector.getInstance(Foo.class);

    //THEN
    assertThat(foo.bar, nullValue());
  }
}
