package toothpick.inject;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPick;
import toothpick.ToothPickBaseTest;
import toothpick.data.Bar;
import toothpick.data.Foo;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class InjectionWithoutModuleTest extends ToothPickBaseTest {

  @Test
  public void testSimpleInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    Foo foo = new Foo();

    //WHEN
    ToothPick.inject(foo, scope);

    //THEN
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }
}
