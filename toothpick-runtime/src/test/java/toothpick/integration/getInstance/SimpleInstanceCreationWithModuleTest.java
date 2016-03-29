package toothpick.integration.getInstance;

import javax.inject.Inject;
import org.junit.Test;
import toothpick.Factory;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.config.Module;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * with a module that binds a single class.
 */
public class SimpleInstanceCreationWithModuleTest {

  @Test public void testSimpleInjection() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, new SimpleModule());

    //WHEN
    Foo instance = injector.createInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
  }

  @Test public void testSimpleInjectionIsNotProducingSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null, new SimpleModule());

    //WHEN
    Foo instance = injector.createInstance(Foo.class);
    Foo instance2 = injector.createInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
    assertThat(instance2, notNullValue());
    assertThat(instance, not(sameInstance(instance2)));
  }

  private static class SimpleModule extends Module {
    public SimpleModule() {
      bind(Foo.class).to(Foo.class);
    }
  }

  public static class Foo {
    @Inject //annotation is not needed, but it's a better example
    public Foo() {
    }
  }

  @SuppressWarnings("unused") public static class Foo$$Factory implements Factory<Foo> {
    @Override public Foo createInstance(Injector injector) {
      return new Foo();
    }

    @Override public boolean hasSingletonAnnotation() {
      return false;
    }

    @Override public boolean hasProducesSingletonAnnotation() {
      return false;
    }
  }
}
