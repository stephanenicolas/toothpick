package toothpick.scoping;

import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
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
    Injector injectorParent = new InjectorImpl("");
    injectorParent.installModules(new Module() {
      {
        bind(Foo.class).to(foo1);
      }
    });
    final Foo foo2 = new Foo();
    Injector injector = new InjectorImpl("");
    injectorParent.addChild(injector);
    injector.installModules(new Module() {
      {
        bind(Foo.class).to(foo2);
      }
    });

    //WHEN
    Foo instance = injector.getInstance(Foo.class);

    //THEN
    assertThat(foo2, sameInstance(instance));
    assertThat(foo2, not(sameInstance(foo1)));
  }

  @Test
  public void childInjector_shouldReturnInstancesInParentScope_whenParentHasKeyInHisScope() throws Exception {
    //GIVEN
    final Foo foo1 = new Foo();
    Injector injectorParent = new InjectorImpl("");
    injectorParent.installModules(new Module() {
      {
        bind(Foo.class).to(foo1);
      }
    });
    Injector injector = new InjectorImpl("");
    injectorParent.addChild(injector);

    //WHEN
    Foo instance = injector.getInstance(Foo.class);
    Foo instance2 = injectorParent.getInstance(Foo.class);

    //THEN
    assertThat(foo1, sameInstance(instance));
    assertThat(foo1, sameInstance(instance2));
  }

  @Test
  public void singletonDiscoveredDynamically_shouldGoInRootScope() throws Exception {
    //GIVEN
    Injector injectorParent = new InjectorImpl("");
    Injector injector = new InjectorImpl("");
    injectorParent.addChild(injector);

    //WHEN
    FooSingleton instance = injector.getInstance(FooSingleton.class);
    FooSingleton instance2 = injectorParent.getInstance(FooSingleton.class);

    //THEN
    assertThat(instance, sameInstance(instance2));
    assertThat(instance, notNullValue());
  }
}
