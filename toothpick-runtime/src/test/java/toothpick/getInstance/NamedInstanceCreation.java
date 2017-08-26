package toothpick.getInstance;

import javax.inject.Provider;
import javax.inject.Qualifier;
import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothpickBaseTest;
import toothpick.config.Module;
import toothpick.data.Foo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/*
 * Creates a instance in the simplest possible way
  * with a module that binds a single class.
 */
public class NamedInstanceCreation extends ToothpickBaseTest {

  static Foo namedFooInstance = new Foo();

  @Test
  public void testNamedInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new SimpleModule());

    //WHEN
    Foo namedInstance = scope.getInstance(Foo.class, "bar");

    //THEN
    assertThat(namedInstance, is(namedFooInstance));
  }

  @Test
  public void testNamedInjection_shouldNotBeConfusedWithUnNamedInjection_whenUsingName() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new SimpleModule());

    //WHEN
    Foo instance = scope.getInstance(Foo.class, "bar");
    Foo instance2 = scope.getInstance(Foo.class, "bar");
    Foo instance3 = scope.getInstance(Foo.class, FooName.class.getName());
    Foo instance4 = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, is(namedFooInstance));
    assertThat(instance2, is(namedFooInstance));
    assertThat(instance3, is(namedFooInstance));
    assertThat(instance4, notNullValue());
    assertThat(instance, sameInstance(instance2));
    assertThat(instance, sameInstance(instance3));
    assertThat(instance, not(sameInstance(instance4)));
  }

  @Test
  public void testNamedProviderInjection_shouldNotBeConfusedWithUnNamedInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    scope.installModules(new SimpleModule());

    //WHEN
    Provider<Foo> provider = scope.getProvider(Foo.class, "bar");
    Provider<Foo> provider2 = scope.getProvider(Foo.class, "bar");
    Provider<Foo> provider3 = scope.getProvider(Foo.class, FooName.class.getName());
    Provider<Foo> provider4 = scope.getProvider(Foo.class);

    //THEN
    assertThat(provider.get(), is(namedFooInstance));
    assertThat(provider2.get(), is(namedFooInstance));
    assertThat(provider3.get(), is(namedFooInstance));
    assertThat(provider4.get(), notNullValue());
    assertThat(provider, not(sameInstance(provider4)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInstallModule_shouldFail_ifModuleUsesClassAsNameButNotAnAnnotation() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");

    //WHEN
    scope.installModules(new Module() {
      {
        bind(Foo.class).withName(NotQualifierAnnotationFooName.class).toInstance(namedFooInstance);
      }
    });

    //THEN
    fail("Should have thrown an exception");
  }

  private static class SimpleModule extends Module {
    SimpleModule() {
      bind(Foo.class).withName("bar").toInstance(namedFooInstance);
      bind(Foo.class).withName(FooName.class).toInstance(namedFooInstance);
      bind(Foo.class).to(Foo.class);
    }
  }

  @Qualifier
  private @interface FooName {
  }

  private @interface NotQualifierAnnotationFooName {
  }
}
