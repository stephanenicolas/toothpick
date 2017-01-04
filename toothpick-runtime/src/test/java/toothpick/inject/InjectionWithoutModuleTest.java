package toothpick.inject;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.Toothpick;
import toothpick.ToothpickBaseTest;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.Foo;
import toothpick.data.FooChildMaskingMember;
import toothpick.data.FooNested;
import toothpick.data.FooParentMaskingMember;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class InjectionWithoutModuleTest extends ToothpickBaseTest {

  @Test
  public void testSimpleInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    Foo foo = new Foo();

    //WHEN
    Toothpick.inject(foo, scope);

    //THEN
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void testNestedClassInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");

    //WHEN
    FooNested fooNested = scope.getInstance(FooNested.class);
    FooNested.InnerClass1 innerClass1 = scope.getInstance(FooNested.InnerClass1.class);
    FooNested.InnerClass1.InnerClass2 innerClass2 = scope.getInstance(FooNested.InnerClass1.InnerClass2.class);

    //THEN
    assertThat(fooNested.bar, notNullValue());
    assertThat(fooNested.bar, isA(Bar.class));
    assertThat(innerClass1.bar, notNullValue());
    assertThat(innerClass1.bar, isA(Bar.class));
    assertThat(innerClass2.bar, notNullValue());
    assertThat(innerClass2.bar, isA(Bar.class));
  }

  @Test
  public void testInjection_shouldFail_whenFieldsAreMasked() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");

    //WHEN
    FooChildMaskingMember fooChildMaskingMember = scope.getInstance(FooChildMaskingMember.class);
    String parentBarToString = fooChildMaskingMember.toString();

    //THEN
    assertThat(parentBarToString, notNullValue());
    assertThat(fooChildMaskingMember.bar, not(sameInstance(((FooParentMaskingMember) fooChildMaskingMember).bar)));
  }

  @Test
  public void testInjection_shouldWork_whenInheritingBinding() throws Exception {
    //GIVEN

    Scope scope = Toothpick.openScope("root");
    scope.installModules(new Module() {
      {
        bind(Foo.class).to(Foo.class);
      }
    });
    Scope childScope = Toothpick.openScopes("root", "child");
    Foo foo = new Foo();

    //WHEN
    Toothpick.inject(foo, childScope);

    //THEN
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  @Test
  public void testInjection_shouldNotThrowAnException_whenNoDependencyIsFound() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("root");
    NotInjectable notInjectable = new NotInjectable();

    //WHEN
    Toothpick.inject(notInjectable, scope);

    //THEN
    // nothing
  }

  class NotInjectable {
  }
}
