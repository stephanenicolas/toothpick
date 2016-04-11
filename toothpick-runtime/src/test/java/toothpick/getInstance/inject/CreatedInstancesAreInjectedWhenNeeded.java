package toothpick.getInstance.inject;

import javax.inject.Provider;
import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.ToothPickBaseTest;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.Foo;
import toothpick.data.FooProvider;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class CreatedInstancesAreInjectedWhenNeeded extends ToothPickBaseTest {

  @Test
  public void createdInstance_shouldBeInjected_whenBindingToAClassWithInjectFields() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl("");
    injector.installModules(new Module() {
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

  @Test
  public void createdInstance_shouldNotBeInjected_whenBindingToAProvider() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl("");
    injector.installModules(new Module() {
      {
        bind(Foo.class).toProvider(new Provider<Foo>() {
          @Override
          public Foo get() {
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

  @Test
  public void createdInstance_shouldNotBeInjected_whenBindingToAProviderClass() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl("");
    injector.installModules(new Module() {
      {
        bind(Foo.class).toProvider(FooProvider.class);
      }
    });

    //WHEN
    Foo foo = injector.getInstance(Foo.class);

    //THEN
    assertThat(foo.bar, nullValue());
  }

  @Test
  public void createdProvider_shouldBeInjected_whenBindingToAProviderClassThatHasInjectedFields() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl("");
    injector.installModules(new Module() {
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
