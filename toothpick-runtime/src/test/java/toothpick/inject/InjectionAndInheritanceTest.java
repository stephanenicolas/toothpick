package toothpick.inject;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPick;
import toothpick.ToothPickBaseTest;
import toothpick.data.Bar;
import toothpick.data.FooChildWithInjectedFields;
import toothpick.data.FooGrandChildWithInjectedFields;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/*
* Test toothpick w.r.t. inheritance of injected classes.
 */
public class InjectionAndInheritanceTest extends ToothPickBaseTest {

  @Test
  public void inject_shouldInjectInheritedFields_whenParentDefinesInjectAnnotatedFields() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    FooChildWithInjectedFields foo = new FooChildWithInjectedFields();

    //WHEN
    ToothPick.inject(foo, scope);

    //THEN
    assertThat(foo.bar2, notNullValue());
    assertThat(foo.bar2, isA(Bar.class));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void inject_shouldInjectInheritedFields_whenGrandParentDefinesInjectAnnotatedFieldsButNotParent() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    FooGrandChildWithInjectedFields foo = new FooGrandChildWithInjectedFields();

    //WHEN
    ToothPick.inject(foo, scope);

    //THEN
    assertThat(foo.bar2, notNullValue());
    assertThat(foo.bar2, isA(Bar.class));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }
}
