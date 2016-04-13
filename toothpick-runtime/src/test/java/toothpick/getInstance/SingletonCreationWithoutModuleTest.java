package toothpick.getInstance;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPickBaseTest;
import toothpick.data.FooSingleton;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a singleton in the simplest possible way
  * without any module.
 */
public class SingletonCreationWithoutModuleTest extends ToothPickBaseTest {

  @Test
  public void testIsProducingSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");

    //WHEN
    FooSingleton instance = scope.getInstance(FooSingleton.class);
    FooSingleton instance2 = scope.getInstance(FooSingleton.class);

    //THEN
    assertThat(instance, notNullValue());
    assertThat(instance2, notNullValue());
    assertThat(instance, sameInstance(instance2));
  }
}
