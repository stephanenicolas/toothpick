package toothpick.getInstance;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPickBaseTest;
import toothpick.config.Module;
import toothpick.data.FooSingleton;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a singleton in the simplest possible way
  * without any module.
 */
public class SingletonCreationWithModuleTest extends ToothPickBaseTest {

  @Test
  public void testIsProducingSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new SimpleModule());

    //WHEN
    FooSingleton instance = scope.getInstance(FooSingleton.class);
    FooSingleton instance2 = scope.getInstance(FooSingleton.class);

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
