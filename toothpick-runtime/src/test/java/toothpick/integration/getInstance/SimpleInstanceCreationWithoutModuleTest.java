package toothpick.integration.getInstance;

import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.ToothPickBaseTest;
import toothpick.integration.data.Foo;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class SimpleInstanceCreationWithoutModuleTest extends ToothPickBaseTest {

  @Test public void testSimpleInjection() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl();

    //WHEN
    Foo instance = injector.getInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
  }

  @Test public void testSimpleInjectionIsNotProducingSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl();

    //WHEN
    Foo instance = injector.getInstance(Foo.class);
    Foo instance2 = injector.getInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
    assertThat(instance2, notNullValue());
    assertThat(instance, not(sameInstance(instance2)));
  }
}
