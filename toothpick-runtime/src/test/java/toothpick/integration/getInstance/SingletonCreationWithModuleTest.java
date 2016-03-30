package toothpick.integration.getInstance;

import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.config.Module;
import toothpick.integration.data.FooSingleton;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a singleton in the simplest possible way
  * without any module.
 */
public class SingletonCreationWithModuleTest {

  @Test public void testIsProducingSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(new SimpleModule());

    //WHEN
    FooSingleton instance = injector.getInstance(FooSingleton.class);
    FooSingleton instance2 = injector.getInstance(FooSingleton.class);

    //THEN
    assertThat(instance, notNullValue());
    assertThat(instance2, notNullValue());
    assertThat(instance, sameInstance(instance2));
  }

  private static class SimpleModule extends Module {
    public SimpleModule() {
      bind(FooSingleton.class);
    }
  }
}
