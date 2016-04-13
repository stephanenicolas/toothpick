package toothpick.scoping;

import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPickBaseTest;
import toothpick.config.Module;
import toothpick.data.Foo;
import toothpick.data.FooSingleton;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Tests scopes related features of toothpick.
 */
public class ScopingTest extends ToothPickBaseTest {

  @Test
  public void childInjector_shouldReturnInstancesInItsScope_whenParentAlsoHasSameKeyInHisScope() throws Exception {
    //GIVEN
    final Foo foo1 = new Foo();
    Scope scopeParent = new ScopeImpl("");
    scopeParent.installModules(new Module() {
      {
        bind(Foo.class).to(foo1);
      }
    });
    final Foo foo2 = new Foo();
    Scope scope = new ScopeImpl("");
    scopeParent.addChild(scope);
    scope.installModules(new Module() {
      {
        bind(Foo.class).to(foo2);
      }
    });

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(foo2, sameInstance(instance));
    assertThat(foo2, not(sameInstance(foo1)));
  }

  @Test
  public void childInjector_shouldReturnInstancesInParentScope_whenParentHasKeyInHisScope() throws Exception {
    //GIVEN
    final Foo foo1 = new Foo();
    Scope scopeParent = new ScopeImpl("");
    scopeParent.installModules(new Module() {
      {
        bind(Foo.class).to(foo1);
      }
    });
    Scope scope = new ScopeImpl("");
    scopeParent.addChild(scope);

    //WHEN
    Foo instance = scope.getInstance(Foo.class);
    Foo instance2 = scopeParent.getInstance(Foo.class);

    //THEN
    assertThat(foo1, sameInstance(instance));
    assertThat(foo1, sameInstance(instance2));
  }

  @Test
  public void singletonDiscoveredDynamically_shouldGoInRootScope() throws Exception {
    //GIVEN
    Scope scopeParent = new ScopeImpl("");
    Scope scope = new ScopeImpl("");
    scopeParent.addChild(scope);

    //WHEN
    FooSingleton instance = scope.getInstance(FooSingleton.class);
    FooSingleton instance2 = scopeParent.getInstance(FooSingleton.class);

    //THEN
    assertThat(instance, sameInstance(instance2));
    assertThat(instance, notNullValue());
  }
}
