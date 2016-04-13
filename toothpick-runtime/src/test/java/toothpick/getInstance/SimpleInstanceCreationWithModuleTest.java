package toothpick.getInstance;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPickBaseTest;
import toothpick.config.Module;
import toothpick.data.Foo;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * with a module that binds a single class.
 */
public class SimpleInstanceCreationWithModuleTest extends ToothPickBaseTest {

  @Test
  public void testSimpleInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl(new SimpleModule());

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
  }

  @Test
  public void testSimpleInjectionIsNotProducingSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl(new SimpleModule());

    //WHEN
    Foo instance = scope.getInstance(Foo.class);
    Foo instance2 = scope.getInstance(Foo.class);

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
