/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.scoping;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static toothpick.configuration.Configuration.forDevelopment;

import org.junit.After;
import org.junit.Test;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.BarChild;
import toothpick.data.CustomScope;
import toothpick.data.Foo;
import toothpick.data.FooChildWithoutInjectedFields;
import toothpick.data.FooCustomScope;
import toothpick.data.FooProviderAnnotatedProvidesSingleton;
import toothpick.data.FooProviderAnnotatedSingleton;
import toothpick.data.FooSingleton;
import toothpick.data.IFoo;
import toothpick.data.IFooProvider;
import toothpick.data.IFooSingleton;

/*
 * Tests scopes related features of toothpick.
 */
public class ScopingTest {

  @After
  public void tearDown() throws Exception {
    Toothpick.reset();
  }

  @Test
  public void childInjector_shouldReturnInstancesInItsScope_whenParentAlsoHasSameKeyInHisScope()
      throws Exception {
    // GIVEN
    final Foo foo1 = new Foo();
    Scope scopeParent = Toothpick.openScope("root");
    scopeParent.installModules(
        new Module() {
          {
            bind(Foo.class).toInstance(foo1);
          }
        });
    final Foo foo2 = new Foo();
    Scope scope = Toothpick.openScopes("root", "child");
    scope.installModules(
        new Module() {
          {
            bind(Foo.class).toInstance(foo2);
          }
        });

    // WHEN
    Foo instance = scope.getInstance(Foo.class);

    // THEN
    assertThat(foo2, sameInstance(instance));
    assertThat(foo2, not(sameInstance(foo1)));
  }

  @Test
  public void childInjector_shouldReturnInstancesInParentScope_whenParentHasKeyInHisScope()
      throws Exception {
    // GIVEN
    final Foo foo1 = new Foo();
    Scope scopeParent = Toothpick.openScope("root");
    scopeParent.installModules(
        new Module() {
          {
            bind(Foo.class).toInstance(foo1);
          }
        });
    Scope scope = Toothpick.openScopes("root", "child");

    // WHEN
    Foo instance = scope.getInstance(Foo.class);
    Foo instance2 = scopeParent.getInstance(Foo.class);

    // THEN
    assertThat(foo1, sameInstance(instance));
    assertThat(foo1, sameInstance(instance2));
  }

  @Test
  public void childInjector_shouldReturnInstancesInParentScopeUsingBindingScope() {
    // GIVEN
    Scope scopeParent = Toothpick.openScope("root");
    scopeParent.installModules(
        new Module() {
          {
            bind(Foo.class).to(FooChildWithoutInjectedFields.class);
          }
        });
    Scope scope = Toothpick.openScopes("root", "child");
    scope.installModules(
        new Module() {
          {
            bind(Bar.class).to(BarChild.class);
          }
        });

    // WHEN
    Foo instance = scope.getInstance(Foo.class);

    // THEN
    assertThat(instance.bar, instanceOf(Bar.class));
    assertThat(instance.bar, not(instanceOf(BarChild.class)));
  }

  @Test
  public void
      childInjector_shouldReturnInstancesInParentScopeUsingChildScope_whenChildOverridesBinding()
          throws Exception {
    // GIVEN
    Scope scopeParent = Toothpick.openScope("root");
    Scope scope = Toothpick.openScopes("root", "child");
    scope.installModules(
        new Module() {
          {
            bind(Bar.class).to(BarChild.class);
          }
        });

    // WHEN
    scopeParent.getInstance(Foo.class); // Create Foo internal provider in parent scope dynamically
    Foo instance = scope.getInstance(Foo.class);

    // THEN
    assertThat(instance.bar, instanceOf(BarChild.class));
  }

  @Test
  public void
      childInjector_shouldReturnInstancesInParentScopeUsingOnlyParentScope_whenSimpleBindingIsCreatinginScope()
          throws Exception {
    // GIVEN
    Scope scopeParent = Toothpick.openScope("root");
    scopeParent.installModules(
        new Module() {
          {
            bind(Foo.class);
          }
        });
    Scope scope = Toothpick.openScopes("root", "child");
    scope.installModules(
        new Module() {
          {
            bind(Bar.class).to(BarChild.class);
          }
        });

    // WHEN
    Foo instance = scope.getInstance(Foo.class);

    // THEN
    assertThat(instance.bar, instanceOf(Bar.class));
    assertThat(instance.bar, not(instanceOf(BarChild.class)));
  }

  @Test
  public void
      childInjector_shouldReturnInstancesInParentScopeUsingOnlyParentScope_whenSimpleBindingIsCreatingSingletonInScope()
          throws Exception {
    // GIVEN
    Scope scopeParent = Toothpick.openScope("root");
    scopeParent.installModules(
        new Module() {
          {
            bind(Foo.class).singleton();
          }
        });
    Scope scope = Toothpick.openScopes("root", "child");
    scope.installModules(
        new Module() {
          {
            bind(Bar.class).to(BarChild.class);
          }
        });

    // WHEN
    Foo instance = scope.getInstance(Foo.class);

    // THEN
    assertThat(instance.bar, instanceOf(Bar.class));
    assertThat(instance.bar, not(instanceOf(BarChild.class)));
  }

  @Test
  public void
      childInjector_shouldReturnInstancesInParentScopeUsingOnlyParentScope_whenClassBindingIsCreatinginScope()
          throws Exception {
    // GIVEN
    Scope scopeParent = Toothpick.openScope("root");
    scopeParent.installModules(
        new Module() {
          {
            bind(IFoo.class).to(Foo.class);
          }
        });
    Scope scope = Toothpick.openScopes("root", "child");
    scope.installModules(
        new Module() {
          {
            bind(Bar.class).to(BarChild.class);
          }
        });

    // WHEN
    IFoo instance = scope.getInstance(IFoo.class);

    // THEN
    assertThat(((Foo) instance).bar, instanceOf(Bar.class));
    assertThat(((Foo) instance).bar, not(instanceOf(BarChild.class)));
  }

  @Test
  public void
      childInjector_shouldReturnInstancesInParentScopeUsingOnlyParentScope_whenClassBindingIsCreatingSingletonInScope()
          throws Exception {
    // GIVEN
    Scope scopeParent = Toothpick.openScope("root");
    scopeParent.installModules(
        new Module() {
          {
            bind(IFoo.class).to(Foo.class).singleton();
          }
        });
    Scope scope = Toothpick.openScopes("root", "child");
    scope.installModules(
        new Module() {
          {
            bind(Bar.class).to(BarChild.class);
          }
        });

    // WHEN
    IFoo instance = scope.getInstance(IFoo.class);

    // THEN
    assertThat(((Foo) instance).bar, instanceOf(Bar.class));
    assertThat(((Foo) instance).bar, not(instanceOf(BarChild.class)));
  }

  @Test
  public void
      childInjector_shouldReturnInstancesInParentScopeUsingOnlyParentScope_whenProviderClassBindingIsCreatinginScope()
          throws Exception {
    // GIVEN
    Scope scopeParent = Toothpick.openScope("root");
    scopeParent.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(IFooProvider.class);
          }
        });
    Scope scope = Toothpick.openScopes("root", "child");
    scope.installModules(
        new Module() {
          {
            bind(Bar.class).to(BarChild.class);
          }
        });

    // WHEN
    IFoo instance = scope.getInstance(IFoo.class);

    // THEN
    assertThat(((Foo) instance).bar, instanceOf(Bar.class));
    assertThat(((Foo) instance).bar, not(instanceOf(BarChild.class)));
  }

  @Test
  public void
      childInjector_shouldReturnInstancesInParentScopeUsingOnlyParentScope_whenProviderClassBindingIsCreatingSingletonInScope()
          throws Exception {
    // GIVEN
    Scope scopeParent = Toothpick.openScope("root");
    scopeParent.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(IFooProvider.class).singleton();
          }
        });
    Scope scope = Toothpick.openScopes("root", "child");
    scope.installModules(
        new Module() {
          {
            bind(Bar.class).to(BarChild.class);
          }
        });

    // WHEN
    IFoo instance = scope.getInstance(IFoo.class);

    // THEN
    assertThat(((Foo) instance).bar, instanceOf(Bar.class));
    assertThat(((Foo) instance).bar, not(instanceOf(BarChild.class)));
  }

  @Test
  public void singletonDiscoveredDynamically_shouldGoInRootScope() throws Exception {
    // GIVEN
    Scope scopeParent = Toothpick.openScope("root");
    Scope scope = Toothpick.openScopes("root", "child");

    // WHEN
    FooSingleton instance = scope.getInstance(FooSingleton.class);
    FooSingleton instance2 = scopeParent.getInstance(FooSingleton.class);

    // THEN
    assertThat(instance, sameInstance(instance2));
    assertThat(instance, notNullValue());
  }

  @Test
  public void singleton_shouldBeSharedBySubscopes() throws Exception {
    // GIVEN
    Scope scope1 = Toothpick.openScopes("root", "scope1");
    Scope scope2 = Toothpick.openScopes("root", "scope2");

    // WHEN
    FooSingleton instance = scope1.getInstance(FooSingleton.class);
    FooSingleton instance2 = scope2.getInstance(FooSingleton.class);

    // THEN
    assertThat(instance, sameInstance(instance2));
  }

  @Test
  public void singleton_shouldBeShared_whenBoundExplicitlyToSingletonAnnotatedClass()
      throws Exception {
    // GIVEN
    Toothpick.setConfiguration(forDevelopment());
    Scope root = Toothpick.openScopes("root");
    root.installModules(
        new Module() {
          {
            bind(IFooSingleton.class).to(FooSingleton.class);
          }
        });

    // WHEN
    IFooSingleton instance = root.getInstance(IFooSingleton.class);
    IFooSingleton instance2 = root.getInstance(IFooSingleton.class);

    // THEN
    assertThat(instance, sameInstance(instance2));
  }

  @Test
  public void singleton_shouldBeShared_whenBoundingIsSimpleOnSingletonAnnotatedClass()
      throws Exception {
    // GIVEN
    Toothpick.setConfiguration(forDevelopment());
    Scope root = Toothpick.openScopes("root");
    root.installModules(
        new Module() {
          {
            bind(FooSingleton.class);
          }
        });

    // WHEN
    FooSingleton instance = root.getInstance(FooSingleton.class);
    FooSingleton instance2 = root.getInstance(FooSingleton.class);

    // THEN
    assertThat(instance, sameInstance(instance2));
  }

  @Test
  public void singleton_shouldNotBeShared_whenBoundingIsSimpleOnScopeAnnotatedClass()
      throws Exception {
    // GIVEN
    Toothpick.setConfiguration(forDevelopment());
    Scope childScope = Toothpick.openScopes("root", "child");
    childScope.supportScopeAnnotation(CustomScope.class);
    childScope.installModules(
        new Module() {
          {
            bind(FooProviderAnnotatedProvidesSingleton.class);
          }
        });

    // WHEN
    FooProviderAnnotatedProvidesSingleton instance =
        childScope.getInstance(FooProviderAnnotatedProvidesSingleton.class);
    FooProviderAnnotatedProvidesSingleton instance2 =
        childScope.getInstance(FooProviderAnnotatedProvidesSingleton.class);

    // THEN
    assertThat(instance, not(sameInstance(instance2)));
  }

  @Test(expected = IllegalStateException.class)
  public void binding_shouldCrashForScopeAnnotatedClass_whenBindingIsSimple() throws Exception {
    // GIVEN
    Toothpick.setConfiguration(forDevelopment());
    Scope scope1 = Toothpick.openScopes("root", "scope1");

    // WHEN
    scope1.installModules(
        new Module() {
          {
            bind(FooSingleton.class);
          }
        });

    // THEN
    fail("This test should throw a IllegalBindingException.");
  }

  @Test(expected = IllegalStateException.class)
  public void binding_shouldCrashForScopeAnnotatedClass_whenBindingToAClass() throws Exception {
    // GIVEN
    Toothpick.setConfiguration(forDevelopment());
    Scope scope1 = Toothpick.openScopes("root", "scope1");

    // WHEN
    scope1.installModules(
        new Module() {
          {
            bind(IFooSingleton.class).to(FooSingleton.class);
          }
        });

    // THEN
    fail("This test should throw a IllegalBindingException.");
  }

  @Test(expected = IllegalStateException.class)
  public void binding_shouldCrashForScopeAnnotatedClass_whenBindingToAProvider() throws Exception {
    // GIVEN
    Toothpick.setConfiguration(forDevelopment());
    Scope scope1 = Toothpick.openScopes("root", "scope1");

    // WHEN
    scope1.installModules(
        new Module() {
          {
            bind(IFoo.class).toProvider(FooProviderAnnotatedSingleton.class);
          }
        });

    // THEN
    fail("This test should throw a IllegalBindingException.");
  }

  @Test
  public void binding_shouldCreateAnnotatedClassInRootScope_whenInjectingSingletonAnnotatedClass()
      throws Exception {
    // GIVEN
    Toothpick.setConfiguration(forDevelopment());
    Scope scopeParent = Toothpick.openScope("root");
    Scope scope1 = Toothpick.openScopes("root", "scope1");

    // WHEN
    FooProviderAnnotatedSingleton instanceInParent =
        scopeParent.getInstance(FooProviderAnnotatedSingleton.class);
    FooProviderAnnotatedSingleton instanceInChild =
        scope1.getInstance(FooProviderAnnotatedSingleton.class);

    // THEN
    assertThat(instanceInParent, sameInstance(instanceInChild));
  }

  @Test
  public void
      binding_shouldCreateAnnotatedClassInScopeThatSupportsScopeAnnotationViaProvider_whenParentScopeSupportsScopeAnnotation()
          throws Exception {
    // GIVEN
    Toothpick.setConfiguration(forDevelopment());
    Scope scopeParent = Toothpick.openScope(CustomScope.class);
    Scope scope1 = Toothpick.openScopes(CustomScope.class, "child");

    // WHEN
    FooProviderAnnotatedProvidesSingleton instanceInParent =
        scopeParent.getInstance(FooProviderAnnotatedProvidesSingleton.class);
    FooProviderAnnotatedProvidesSingleton instanceInChild =
        scope1.getInstance(FooProviderAnnotatedProvidesSingleton.class);

    // THEN
    assertThat(instanceInParent, not(sameInstance(instanceInChild)));
  }

  @Test
  public void
      binding_shouldCreateAnnotatedClassInScopeThatSupportsScopeAnnotationViaFactory_whenParentScopeSupportsScopeAnnotation()
          throws Exception {
    // GIVEN
    Toothpick.setConfiguration(forDevelopment());
    Scope scopeParent = Toothpick.openScope(CustomScope.class);
    Scope scope1 = Toothpick.openScopes(CustomScope.class, "child");

    // WHEN
    FooCustomScope instanceInParent = scopeParent.getInstance(FooCustomScope.class);
    FooCustomScope instanceInChild = scope1.getInstance(FooCustomScope.class);

    // THEN
    assertThat(instanceInParent, not(sameInstance(instanceInChild)));
  }
}
