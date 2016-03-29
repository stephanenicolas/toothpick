package toothpick.integration.scoping;

import javax.inject.Singleton;
import org.junit.Test;
import toothpick.Factory;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.MemberInjector;
import toothpick.config.Module;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Tests scopes related features of toothpick.
 */
public class ScopingTest {

  @Test public void childInjector_shouldReturnInstancesInItsScope_whenParentAlsoHasSameKeyInHisScope() throws Exception {
    //GIVEN
    final Foo foo1 = new Foo();
    Injector injectorParent = new InjectorImpl(new Module() {
      {
        bind(Foo.class).to(foo1);
      }
    });
    final Foo foo2 = new Foo();
    Injector injector = new InjectorImpl(injectorParent, new Module() {
      {
        bind(Foo.class).to(foo2);
      }
    });

    //WHEN
    Foo instance = injector.createInstance(Foo.class);

    //THEN
    assertThat(foo2, sameInstance(instance));
    assertThat(foo2, not(sameInstance(foo1)));
  }

  @Test public void childInjector_shouldReturnInstancesInParentScope_whenParentHasKeyInHisScope() throws Exception {
    //GIVEN
    final Foo foo1 = new Foo();
    Injector injectorParent = new InjectorImpl(new Module() {
      {
        bind(Foo.class).to(foo1);
      }
    });
    Injector injector = new InjectorImpl(injectorParent);

    //WHEN
    Foo instance = injector.createInstance(Foo.class);
    Foo instance2 = injectorParent.createInstance(Foo.class);

    //THEN
    assertThat(foo1, sameInstance(instance));
    assertThat(foo1, sameInstance(instance2));
  }

  @Test public void singletonDiscoveredDynamically_shouldGoInRootScope() throws Exception {
    //GIVEN
    Injector injectorParent = new InjectorImpl();
    Injector injector = new InjectorImpl(injectorParent);

    //WHEN
    FooSingleton instance = injector.createInstance(FooSingleton.class);
    FooSingleton instance2 = injectorParent.createInstance(FooSingleton.class);

    //THEN
    assertThat(instance, sameInstance(instance2));
    assertThat(instance, notNullValue());
  }

  public static class Foo {
  }

  @Singleton //not used really but more clear to demonstrate what we do
  public static class FooSingleton {
  }

  @SuppressWarnings("unused") public static class FooSingleton$$Factory implements Factory<FooSingleton> {
    @Override public FooSingleton createInstance(Injector injector) {
      return new FooSingleton();
    }

    @Override public boolean hasSingletonAnnotation() {
      return true;
    }

    @Override public boolean hasProducesSingletonAnnotation() {
      return false;
    }
  }

  @SuppressWarnings("unused") public static class FooSingleton$$MemberInjector implements MemberInjector<FooSingleton> {

    @Override public void inject(FooSingleton fooSingleton, Injector injector) {

    }
  }
}
