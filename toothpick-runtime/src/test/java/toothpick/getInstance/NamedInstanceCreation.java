package toothpick.getInstance;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPickBaseTest;
import toothpick.config.Module;
import toothpick.data.Foo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * with a module that binds a single class.
 */
public class NamedInstanceCreation extends ToothPickBaseTest {

  static Foo namedFooInstance = new Foo();

  @Test
  public void testSimpleInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new SimpleModule());

    //WHEN
    Foo namedInstance = scope.getInstance(Foo.class, "bar");

    //THEN
    assertThat(namedInstance, is(namedFooInstance));
  }

  @Test
  public void testSimpleInjectionIsNotProducingSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new SimpleModule());

    //WHEN
    Foo instance = scope.getInstance(Foo.class, "bar");
    Foo instance2 = scope.getInstance(Foo.class, "bar");
    Foo instance3 = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, is(namedFooInstance));
    assertThat(instance2, is(namedFooInstance));
    assertThat(instance3, notNullValue());
    assertThat(instance, sameInstance(instance2));
    assertThat(instance, not(sameInstance(instance3)));
  }

  private static class SimpleModule extends Module {
    public SimpleModule() {
      bind(Foo.class).withName("bar").to(namedFooInstance);
      bind(Foo.class).to(Foo.class);
    }
  }
}
