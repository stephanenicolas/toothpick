package toothpick.integration.inject;

import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.integration.ToothPickIntegrationTest;
import toothpick.integration.data.Bar;
import toothpick.integration.data.FooChildWithInjectedFields;
import toothpick.integration.data.FooGrandChildWithInjectedFields;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/*
* Test toothpick w.r.t. inheritance of injected classes.
 */
public class InjectionAndInheritanceTest extends ToothPickIntegrationTest {

  @Test public void inject_shouldInjectInheritedFields_whenParentDefinesInjectAnnotatedFields() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl();
    FooChildWithInjectedFields foo = new FooChildWithInjectedFields();

    //WHEN
    injector.inject(foo);

    //THEN
    assertThat(foo.bar2, notNullValue());
    assertThat(foo.bar2, isA(Bar.class));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test public void inject_shouldInjectInheritedFields_whenGrandParentDefinesInjectAnnotatedFieldsButNotParent() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl();
    FooGrandChildWithInjectedFields foo = new FooGrandChildWithInjectedFields();

    //WHEN
    injector.inject(foo);

    //THEN
    assertThat(foo.bar2, notNullValue());
    assertThat(foo.bar2, isA(Bar.class));
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }
}
