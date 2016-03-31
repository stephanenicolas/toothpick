package toothpick.integration.getInstance;

import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.config.Module;
import toothpick.integration.ToothPickIntegrationTest;
import toothpick.integration.data.Foo;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * with a module that binds a single class.
 */
public class SimpleInstanceCreationWithModuleTest extends ToothPickIntegrationTest {

  @Test public void testSimpleInjection() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new SimpleModule());

    //WHEN
    Foo instance = injector.getInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
  }

  @Test public void testSimpleInjectionIsNotProducingSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new SimpleModule());

    //WHEN
    Foo instance = injector.getInstance(Foo.class);
    Foo instance2 = injector.getInstance(Foo.class);

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
}
