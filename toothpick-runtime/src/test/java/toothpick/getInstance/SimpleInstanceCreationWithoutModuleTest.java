package toothpick.getInstance;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPickBaseTest;
import toothpick.data.Foo;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class SimpleInstanceCreationWithoutModuleTest extends ToothPickBaseTest {

  @Test
  public void testSimpleInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
  }

  @Test
  public void testSimpleInjectionIsNotProducingSingleton() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");

    //WHEN
    Foo instance = scope.getInstance(Foo.class);
    Foo instance2 = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
    assertThat(instance2, notNullValue());
    assertThat(instance, not(sameInstance(instance2)));
  }
}
