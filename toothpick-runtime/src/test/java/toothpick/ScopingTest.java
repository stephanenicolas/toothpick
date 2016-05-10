package toothpick;

import org.junit.Test;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.BarChild;
import toothpick.data.CustomScope;
import toothpick.data.Foo;
import toothpick.data.FooChildWithoutInjectedFields;
import toothpick.data.FooSingleton;
import toothpick.data.IFoo;
import toothpick.data.IFooProviderAnnotatedProvidesSingleton;
import toothpick.data.IFooProviderAnnotatedSingleton;
import toothpick.data.IFooSingleton;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static toothpick.Configuration.development;

/*
 * Tests scopes related features of toothpick.
 */
public class ScopingTest extends ToothPickBaseTest {

  @Test
  public void childInjector_shouldReturnInstancesInItsScope_whenParentAlsoHasSameKeyInHisScope() throws Exception {
    //GIVEN
    final Foo foo1 = new Foo();
    ScopeNode scopeParent = new ScopeImpl("");
    scopeParent.installModules(new Module() {
      {
        bind(Foo.class).to(foo1);
      }
    });
    final Foo foo2 = new Foo();
    ScopeNode scope = new ScopeImpl("");
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
    ScopeNode scopeParent = new ScopeImpl("");
    scopeParent.installModules(new Module() {
      {
        bind(Foo.class).to(foo1);
      }
    });
    ScopeNode scope = new ScopeImpl("");
    scopeParent.addChild(scope);

    //WHEN
    Foo instance = scope.getInstance(Foo.class);
    Foo instance2 = scopeParent.getInstance(Foo.class);

    //THEN
    assertThat(foo1, sameInstance(instance));
    assertThat(foo1, sameInstance(instance2));
  }

  @Test
  public void childInjector_shouldReturnInstancesInParentScopeUsingChildScope_whenParentHasKeyInHisScopeThroughModule() throws Exception {
    //GIVEN
    ScopeNode scopeParent = new ScopeImpl("");
    scopeParent.installModules(new Module() {
      {
        bind(Foo.class).to(FooChildWithoutInjectedFields.class);
      }
    });
    ScopeNode scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(Bar.class).to(BarChild.class);
      }
    });
    scopeParent.addChild(scope);

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance.bar, instanceOf(BarChild.class));
  }

  @Test
  public void childInjector_shouldReturnInstancesInParentScopeUsingChildScope_whenChildOverridesBinding() throws Exception {
    //GIVEN
    ScopeNode scopeParent = new ScopeImpl("");
    ScopeNode scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(Bar.class).to(BarChild.class);
      }
    });
    scopeParent.addChild(scope);

    //WHEN
    scopeParent.getInstance(Foo.class); // Create Foo internal provider in parent scope dynamically
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance.bar, instanceOf(BarChild.class));
  }

  @Test
  public void childInjector_shouldReturnInstancesInParentScopeUsingOnlyParentScope_whenBindingIsScoped() throws Exception {
    //GIVEN
    ScopeNode scopeParent = new ScopeImpl("");
    scopeParent.installModules(new Module() {
      {
        bind(Foo.class).scope();
      }
    });
    ScopeNode scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(Bar.class).to(BarChild.class).scope();
      }
    });
    scopeParent.addChild(scope);

    //WHEN
    scopeParent.getInstance(Foo.class); // Create Foo internal provider in parent scope dynamically
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance.bar, instanceOf(Bar.class));
  }

  @Test
  public void singletonDiscoveredDynamically_shouldGoInRootScope() throws Exception {
    //GIVEN
    ScopeNode scopeParent = new ScopeImpl("");
    ScopeNode scope = new ScopeImpl("");
    scopeParent.addChild(scope);

    //WHEN
    FooSingleton instance = scope.getInstance(FooSingleton.class);
    FooSingleton instance2 = scopeParent.getInstance(FooSingleton.class);

    //THEN
    assertThat(instance, sameInstance(instance2));
    assertThat(instance, notNullValue());
  }

  @Test
  public void singleton_shouldBeSharedBySubscopes() throws Exception {
    //GIVEN
    ScopeNode scopeParent = new ScopeImpl("");
    ScopeNode scope1 = new ScopeImpl("scope1");
    ScopeNode scope2 = new ScopeImpl("scope2");
    scopeParent.addChild(scope1);
    scopeParent.addChild(scope2);

    //WHEN
    FooSingleton instance = scope1.getInstance(FooSingleton.class);
    FooSingleton instance2 = scope2.getInstance(FooSingleton.class);

    //THEN
    assertThat(instance, sameInstance(instance2));
  }

  @Test(expected = IllegalBindingException.class)
  public void binding_shouldCrashForScopeAnnotatedClass_whenBindingIsSimple() throws Exception {
    //GIVEN
    ToothPick.setConfiguration(development());
    ScopeNode scopeParent = new ScopeImpl("");
    ScopeNode scope1 = new ScopeImpl("scope1");
    ScopeNode scope2 = new ScopeImpl("scope2");
    scopeParent.addChild(scope1);
    scopeParent.addChild(scope2);

    //WHEN
    scope1.installModules(new Module() {
      {
        bind(FooSingleton.class);
      }
    });

    //THEN
    fail("This test should throw a IllegalBindingException.");
  }

  @Test(expected = IllegalBindingException.class)
  public void binding_shouldCrashForScopeAnnotatedClass_whenBindingToAClass() throws Exception {
    //GIVEN
    ToothPick.setConfiguration(development());
    ScopeNode scopeParent = new ScopeImpl("");
    ScopeNode scope1 = new ScopeImpl("scope1");
    ScopeNode scope2 = new ScopeImpl("scope2");
    scopeParent.addChild(scope1);
    scopeParent.addChild(scope2);

    //WHEN
    scope1.installModules(new Module() {
      {
        bind(IFooSingleton.class).to(FooSingleton.class);
      }
    });

    //THEN
    fail("This test should throw a IllegalBindingException.");
  }

  @Test(expected = IllegalBindingException.class)
  public void binding_shouldCrashForScopeAnnotatedClass_whenBindingToAProvider() throws Exception {
    //GIVEN
    ToothPick.setConfiguration(development());
    ScopeNode scopeParent = new ScopeImpl("");
    ScopeNode scope1 = new ScopeImpl("");
    ScopeNode scope2 = new ScopeImpl("");
    scopeParent.addChild(scope1);
    scopeParent.addChild(scope2);

    //WHEN
    scope1.installModules(new Module() {
      {
        bind(IFoo.class).toProvider(IFooProviderAnnotatedSingleton.class);
      }
    });

    //THEN
    fail("This test should throw a IllegalBindingException.");
  }

  @Test
  public void binding_shouldCreateAnnotatedClassInRootScope_whenInjectingSingletonAnnotatedClass() throws Exception {
    //GIVEN
    ToothPick.setConfiguration(development());
    ScopeNode scopeParent = new ScopeImpl("root");
    ScopeNode scope1 = new ScopeImpl("child");
    scopeParent.addChild(scope1);

    //WHEN
    IFooProviderAnnotatedSingleton instanceInParent = scopeParent.getInstance(IFooProviderAnnotatedSingleton.class);
    IFooProviderAnnotatedSingleton instanceInChild = scope1.getInstance(IFooProviderAnnotatedSingleton.class);

    //THEN
    assertThat(instanceInParent, sameInstance(instanceInChild));
  }

  @Test
  public void binding_shouldCreateAnnotatedClassInScopeBoundToScopeAnnotation_whenParentScopeIsBoundToScopeAnnotation() throws Exception {
    //GIVEN
    ToothPick.setConfiguration(development());
    ScopeNode scopeParent = new ScopeImpl(CustomScope.class);
    ScopeNode scope1 = new ScopeImpl("child");
    scopeParent.addChild(scope1);

    //WHEN
    IFooProviderAnnotatedProvidesSingleton instanceInParent = scopeParent.getInstance(IFooProviderAnnotatedProvidesSingleton.class);
    IFooProviderAnnotatedProvidesSingleton instanceInChild = scope1.getInstance(IFooProviderAnnotatedProvidesSingleton.class);

    //THEN
    assertThat(instanceInParent, sameInstance(instanceInChild));
  }
}
