package toothpick.integration.getInstance;

import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.integration.ToothPickIntegrationTest;
import toothpick.integration.data.FooSingleton;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a singleton in the simplest possible way
  * without any module.
 */
public class SingletonCreationWithoutModuleTest extends ToothPickIntegrationTest {

  @Test public void testIsProducingSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl();

    //WHEN
    FooSingleton instance = injector.getInstance(FooSingleton.class);
    FooSingleton instance2 = injector.getInstance(FooSingleton.class);

    //THEN
    assertThat(instance, notNullValue());
    assertThat(instance2, notNullValue());
    assertThat(instance, sameInstance(instance2));
  }
}
