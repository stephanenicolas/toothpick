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
package toothpick;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.inject.Singleton;
import org.junit.After;
import org.junit.Test;
import toothpick.data.CustomScope;
import toothpick.data.NotAScope;

public class ScopeNodeTest {

  @After
  public void tearDown() throws Exception {
    Toothpick.reset();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateScope_shouldFail_whenNameIsNull() {
    // GIVEN

    // WHEN
    Scope scope = new ScopeImpl(null);

    // THEN
    assertThat(scope, is(scope));
  }

  @Test
  public void testCreateScope_shouldBindToScopeAnnotation_whenNameIsAScopeAnnotation() {
    // GIVEN
    Scope scope = new ScopeImpl(CustomScope.class);

    // WHEN
    boolean isSupportingToScopeAnnotation = scope.isScopeAnnotationSupported(CustomScope.class);

    // THEN
    assertThat(isSupportingToScopeAnnotation, is(true));
  }

  @Test
  public void testCreateScope_shouldNotBindToScopeAnnotation_whenNameIsNotAScopeAnnotation() {
    // GIVEN
    Scope scope = new ScopeImpl(NotAScope.class);

    // WHEN
    boolean isSupportingToScopeAnnotation = scope.isScopeAnnotationSupported(CustomScope.class);

    // THEN
    assertThat(isSupportingToScopeAnnotation, is(false));
  }

  @Test
  public void testGetParentScope_shouldReturnRootScope_whenAskedForSingleton() {
    // GIVEN
    Scope parentScope = Toothpick.openScope("root");
    Scope childScope = Toothpick.openScopes("root", "child");

    // WHEN
    Scope scope = childScope.getParentScope(Singleton.class);

    // THEN
    assertThat(scope, is(parentScope));
  }

  @Test
  public void testGetParentScope_shouldReturnItself_whenScopedToScopeAnnotation() {
    // GIVEN
    Scope childScope = Toothpick.openScopes("root", "child");
    childScope.supportScopeAnnotation(CustomScope.class);

    // WHEN
    Scope scope = childScope.getParentScope(CustomScope.class);

    // THEN
    assertThat(scope, is(childScope));
  }

  @Test
  public void testGetParentScope_shouldReturnParentScope_whenParentSupportsScopeAnnotation() {
    // GIVEN
    Scope parentScope = Toothpick.openScope("root");
    parentScope.supportScopeAnnotation(CustomScope.class);
    Scope childScope = Toothpick.openScopes("root", "child");

    // WHEN
    Scope scope = childScope.getParentScope(CustomScope.class);

    // THEN
    assertThat(scope, is(parentScope));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetParentScope_shouldFail_whenNoParentSupportsScopeAnnotation() {
    // GIVEN
    Scope scope = Toothpick.openScope("root");

    // WHEN
    scope.getParentScope(CustomScope.class);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetParentScope_shouldFail_WhenAnnotationIsNotAScope() {
    // GIVEN
    Scope scope = Toothpick.openScope("root");

    // WHEN
    scope.getParentScope(NotAScope.class);

    // THEN
    fail("Should throw an exception");
  }

  @Test
  public void testGetRootScope_shouldReturnNodeItselfI_whenRoot() {
    // GIVEN
    Scope parentScope = Toothpick.openScope("root");

    // WHEN
    Scope scope = parentScope.getRootScope();

    // THEN
    assertThat(scope, is(parentScope));
  }

  @Test
  public void testGetRootScope_shouldReturnRootScope_whenHasParent() {
    // GIVEN
    Scope parentScope = Toothpick.openScope("root");
    Scope childScope = Toothpick.openScopes("root", "child");

    // WHEN
    Scope scope = childScope.getRootScope();

    // THEN
    assertThat(scope, is(parentScope));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBindScopeAnnotation_shouldFail_whenSingleton() {
    // GIVEN
    Scope scope = Toothpick.openScope("root");

    // WHEN
    scope.supportScopeAnnotation(Singleton.class);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBindScopeAnnotation_shouldFail_whenAnnotationIsNotAScope() {
    // GIVEN
    Scope scope = Toothpick.openScope("root");

    // WHEN
    scope.supportScopeAnnotation(NotAScope.class);

    // THEN
    fail("Should throw an exception");
  }

  @Test
  public void testSupportScopeAnnotation_shouldReturnFalse_whenNotSupported() {
    // GIVEN
    Scope scope = Toothpick.openScope("root");

    // WHEN
    boolean isSupportingToScopeAnnotation = scope.isScopeAnnotationSupported(CustomScope.class);

    // THEN
    assertThat(isSupportingToScopeAnnotation, is(false));
  }

  @Test
  public void testSupportScopeAnnotation_shouldReturnTrue_whenSupported() {
    // GIVEN
    Scope parentScope = Toothpick.openScope("root");
    parentScope.supportScopeAnnotation(CustomScope.class);

    // WHEN
    boolean isSupportingToScopeAnnotation =
        parentScope.isScopeAnnotationSupported(CustomScope.class);

    // THEN
    assertThat(isSupportingToScopeAnnotation, is(true));
  }

  @Test
  public void testSupportScopeAnnotation_shouldReturnTrue_whenRootScopeAskedForSingleton() {
    // GIVEN
    Scope parentScope = Toothpick.openScope("root");

    // WHEN
    boolean isSupportingSingleton = parentScope.isScopeAnnotationSupported(Singleton.class);

    // THEN
    assertThat(isSupportingSingleton, is(true));
  }

  @Test
  public void testSupportScopeAnnotation_shouldReturnFalse_whenNonRootScopeAskedForSingleton() {
    // GIVEN
    Scope childScope = Toothpick.openScopes("root", "child");

    // WHEN
    boolean isSupportingSingleton = childScope.isScopeAnnotationSupported(Singleton.class);

    // THEN
    assertThat(isSupportingSingleton, is(false));
  }

  @Test
  public void testGetChildrenScopes_shouldReturnEmptyList_whenHasNoChildren() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");

    // WHEN
    boolean hasNoChildren = parentScope.getChildrenScopes().isEmpty();

    // THEN
    assertThat(hasNoChildren, is(true));
  }

  @Test
  public void testGetChildrenScopes_shouldReturnChildren_whenHasChildren() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    // WHEN
    Collection<ScopeNode> childrenScopes = parentScope.getChildrenScopes();

    // THEN
    assertThat(childrenScopes.isEmpty(), is(false));
    assertThat(childrenScopes.size(), is(1));
    assertThat(childrenScopes.iterator().next(), is(childScope));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddChild_shouldFail_whenChildIsNull() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");

    // WHEN
    parentScope.addChild(null);

    // THEN
    fail("Should throw an exception");
  }

  @Test
  public void testAddChild_shouldReturnChild() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    // WHEN
    ScopeNode child = parentScope.addChild(childScope);

    // THEN
    assertThat(child, is(childScope));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddChild_shouldFail_whenChildAlreadyHasParent() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode parentScope2 = new ScopeImpl("foo");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    // WHEN
    parentScope2.addChild(childScope);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveChild_shouldFail_whenChildIsNull() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");

    // WHEN
    parentScope.removeChild(null);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveChild_shouldFail_whenChildHasNoParent() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");

    // WHEN
    parentScope.removeChild(childScope);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveChild_shouldFail_whenChildHasDifferentParent() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode parentScope2 = new ScopeImpl("foo");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    // WHEN
    parentScope2.removeChild(childScope);

    // THEN
    fail("Should throw an exception");
  }

  @Test
  public void testRemoveChild() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    // WHEN
    parentScope.removeChild(childScope);

    // WHEN
    Collection<ScopeNode> childrenScopes = parentScope.getChildrenScopes();

    // THEN
    assertThat(childrenScopes.isEmpty(), is(true));
  }

  @Test
  public void testEqualsAndHashCode_shouldReturnTrue_whenNodeHasSameName() {
    // GIVEN
    Scope scope = new ScopeImpl("foo");
    Scope scope2 = new ScopeImpl("foo");

    // WHEN
    boolean equals = scope.equals(scope2);
    boolean equals2 = scope2.equals(scope);
    int hashScope = scope.hashCode();
    int hashScope2 = scope2.hashCode();

    // THEN
    assertThat(equals, is(true));
    assertThat(equals2, is(true));
    assertThat(hashScope, is(hashScope2));
  }

  @Test
  public void testEqualsAndHashCode_shouldReturnFalse_whenNodeHasDifferentName() {
    // GIVEN
    Scope scope = new ScopeImpl("foo");
    Scope scope2 = new ScopeImpl("bar");

    // WHEN
    boolean equals = scope.equals(scope2);
    int hashScope = scope.hashCode();
    int hashScope2 = scope2.hashCode();

    // THEN
    assertThat(equals, is(false));
    assertThat(hashScope, not(is(hashScope2)));
  }

  @Test
  public void testGetParentScopeNames_shouldReturnParentNames_whenThereAreParents() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    // WHEN
    final List<Object> parentScopesNames = childScope.getParentScopesNames();

    // THEN
    assertThat(parentScopesNames.size(), is(1));
    assertThat(parentScopesNames.iterator().next(), is(parentScope.getName()));
  }

  @Test
  public void testGetParentScopeNames_shouldReturnParentNamesInOrder_whenThereAreParents() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");
    ScopeNode grandChildScope = new ScopeImpl("grandChild");
    parentScope.addChild(childScope);
    childScope.addChild(grandChildScope);

    // WHEN
    final List<Object> grandParentScopesNames = grandChildScope.getParentScopesNames();

    // THEN
    assertThat(grandParentScopesNames.size(), is(2));
    final Iterator<Object> iterator = grandParentScopesNames.iterator();
    assertThat(iterator.next(), is(childScope.getName()));
    assertThat(iterator.next(), is(parentScope.getName()));
  }

  @Test
  public void testGetParentScopeNames_shouldReturnParentNames_whenThereAreNoParents() {
    // GIVEN
    ScopeNode parentScope = new ScopeImpl("root");

    // WHEN
    final List<Object> parentScopesNames = parentScope.getParentScopesNames();

    // THEN
    assertThat(parentScopesNames.size(), is(0));
  }

  @Test
  public void testReset_shouldClearSupportedAnnotations_andFlagTheScopeAsOpen() throws Exception {
    // GIVEN
    ScopeNode scope = new ScopeImpl("root");
    scope.supportScopeAnnotation(CustomScope.class);
    scope.close();

    // WHEN
    scope.reset();

    // THEN
    assertThat(scope.isScopeAnnotationSupported(CustomScope.class), is(false));
    assertThat(scope.isOpen, is(true));
  }

  @Test
  public void testReset_shouldRebindScopeAnnotation() throws Exception {
    // GIVEN
    ScopeNode scope = new ScopeImpl(CustomScope.class);

    // WHEN
    scope.reset();

    // THEN
    assertThat(scope.isScopeAnnotationSupported(CustomScope.class), is(true));
  }
}
