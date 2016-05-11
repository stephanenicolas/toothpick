package toothpick;

import java.util.Collection;
import javax.inject.Singleton;
import org.junit.After;
import org.junit.Test;
import toothpick.data.CustomScope;
import toothpick.data.NotAScope;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ScopeNodeTest {

  @After
  public void tearDown() throws Exception {
    ToothPick.reset();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateScope_shouldFail_whenNameIsNull() {
    //GIVEN

    //WHEN
    Scope scope = new ScopeImpl(null);

    //THEN
    assertThat(scope, is(scope));
  }

  @Test
  public void testCreateScope_shouldBindToScopeAnnotation_whenNameIsAScopeAnnotation() {
    //GIVEN
    Scope scope = new ScopeImpl(CustomScope.class);

    //WHEN
    boolean isBoundToScopeAnnotation = scope.isBoundToScopeAnnotation(CustomScope.class);

    //THEN
    assertThat(isBoundToScopeAnnotation, is(true));
  }

  @Test
  public void testCreateScope_shouldNotBindToScopeAnnotation_whenNameIsNotAScopeAnnotation() {
    //GIVEN
    Scope scope = new ScopeImpl(NotAScope.class);

    //WHEN
    boolean isBoundToScopeAnnotation = scope.isBoundToScopeAnnotation(CustomScope.class);

    //THEN
    assertThat(isBoundToScopeAnnotation, is(false));
  }

  @Test
  public void testGetParentScope_shouldReturnRootScope_whenAskedForSingleton() {
    //GIVEN
    Scope parentScope = ToothPick.openScope("root");
    Scope childScope = ToothPick.openScopes("root", "child");

    //WHEN
    Scope scope = childScope.getParentScope(Singleton.class);

    //THEN
    assertThat(scope, is(parentScope));
  }

  @Test
  public void testGetParentScope_shouldReturnItself_whenBoundToScopeAnnotation() {
    //GIVEN
    Scope childScope = ToothPick.openScopes("root", "child");
    childScope.bindScopeAnnotation(CustomScope.class);

    //WHEN
    Scope scope = childScope.getParentScope(CustomScope.class);

    //THEN
    assertThat(scope, is(childScope));
  }

  @Test
  public void testGetParentScope_shouldReturnParentScope_whenParentBoundToScopeAnnotation() {
    //GIVEN
    Scope parentScope = ToothPick.openScope("root");
    parentScope.bindScopeAnnotation(CustomScope.class);
    Scope childScope = ToothPick.openScopes("root", "child");

    //WHEN
    Scope scope = childScope.getParentScope(CustomScope.class);

    //THEN
    assertThat(scope, is(parentScope));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetParentScope_shouldFail_whenNoParentBoundToScopeAnnotation() {
    //GIVEN
    Scope scope = ToothPick.openScope("root");

    //WHEN
    scope.getParentScope(CustomScope.class);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetParentScope_shouldFail_WhenAnnotationIsNotAScope() {
    //GIVEN
    Scope scope = ToothPick.openScope("root");

    //WHEN
    scope.getParentScope(NotAScope.class);

    //THEN
    fail("Should throw an exception");
  }

  @Test
  public void testGetRootScope_shouldReturnNodeItselfI_whenRoot() {
    //GIVEN
    Scope parentScope = ToothPick.openScope("root");

    //WHEN
    Scope scope = parentScope.getRootScope();

    //THEN
    assertThat(scope, is(parentScope));
  }

  @Test
  public void testGetRootScope_shouldReturnRootScope_whenHasParent() {
    //GIVEN
    Scope parentScope = ToothPick.openScope("root");
    Scope childScope = ToothPick.openScopes("root", "child");

    //WHEN
    Scope scope = childScope.getRootScope();

    //THEN
    assertThat(scope, is(parentScope));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBindScopeAnnotation_shouldFail_whenSingleton() {
    //GIVEN
    Scope scope = ToothPick.openScope("root");

    //WHEN
    scope.bindScopeAnnotation(Singleton.class);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBindScopeAnnotation_shouldFail_whenAnnotationIsNotAScope() {
    //GIVEN
    Scope scope = ToothPick.openScope("root");

    //WHEN
    scope.bindScopeAnnotation(NotAScope.class);

    //THEN
    fail("Should throw an exception");
  }

  @Test
  public void testBindScopeAnnotation_shouldReturnFalse_whenNotBound() {
    //GIVEN
    Scope scope = ToothPick.openScope("root");

    //WHEN
    boolean isBoundToScopeAnnotation = scope.isBoundToScopeAnnotation(CustomScope.class);

    //THEN
    assertThat(isBoundToScopeAnnotation, is(false));
  }

  @Test
  public void testBindScopeAnnotation_shouldReturnTrue_whenBound() {
    //GIVEN
    Scope parentScope = ToothPick.openScope("root");
    parentScope.bindScopeAnnotation(CustomScope.class);

    //WHEN
    boolean isBoundToScopeAnnotation = parentScope.isBoundToScopeAnnotation(CustomScope.class);

    //THEN
    assertThat(isBoundToScopeAnnotation, is(true));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBindScopeAnnotation_shouldFail_whenAskedForSingleton() {
    //GIVEN
    Scope parentScope = ToothPick.openScope("root");

    //WHEN
    parentScope.isBoundToScopeAnnotation(Singleton.class);

    //THEN
    fail("Should throw an exception");
  }

  @Test
  public void testGetChildrenScopes_shouldReturnEmptyList_whenHasNoChildren() {
    //GIVEN
    ScopeNode parentScope = new ScopeImpl("root");

    //WHEN
    boolean hasNoChildren = parentScope.getChildrenScopes().isEmpty();

    //THEN
    assertThat(hasNoChildren, is(true));
  }

  @Test
  public void testGetChildrenScopes_shouldReturnChildren_whenHasChildren() {
    //GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    //WHEN
    Collection<ScopeNode> childrenScopes = parentScope.getChildrenScopes();

    //THEN
    assertThat(childrenScopes.isEmpty(), is(false));
    assertThat(childrenScopes.size(), is(1));
    assertThat(childrenScopes.iterator().next(), is(childScope));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddChild_shouldFail_whenChildIsNull() {
    //GIVEN
    ScopeNode parentScope = new ScopeImpl("root");

    //WHEN
    parentScope.addChild(null);

    //THEN
    fail("Should throw an exception");
  }

  @Test
  public void testAddChild_shouldReturnChild() {
    //GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    //WHEN
    ScopeNode child = parentScope.addChild(childScope);

    //THEN
    assertThat(child, is(childScope));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddChild_shouldFail_whenChildAlreadyHasParent() {
    //GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode parentScope2 = new ScopeImpl("foo");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    //WHEN
    parentScope2.addChild(childScope);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveChild_shouldFail_whenChildIsNull() {
    //GIVEN
    ScopeNode parentScope = new ScopeImpl("root");

    //WHEN
    parentScope.removeChild(null);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveChild_shouldFail_whenChildHasNoParent() {
    //GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");

    //WHEN
    parentScope.removeChild(childScope);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveChild_shouldFail_whenChildHasDifferentParent() {
    //GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode parentScope2 = new ScopeImpl("foo");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    //WHEN
    parentScope2.removeChild(childScope);

    //THEN
    fail("Should throw an exception");
  }

  @Test
  public void testRemoveChild() {
    //GIVEN
    ScopeNode parentScope = new ScopeImpl("root");
    ScopeNode childScope = new ScopeImpl("child");
    parentScope.addChild(childScope);

    //WHEN
    parentScope.removeChild(childScope);

    //WHEN
    Collection<ScopeNode> childrenScopes = parentScope.getChildrenScopes();

    //THEN
    assertThat(childrenScopes.isEmpty(), is(true));
  }

  @Test
  public void testEqualsAndHashCode_shouldReturnTrue_whenNodeHasSameName() {
    //GIVEN
    Scope scope = new ScopeImpl("foo");
    Scope scope2 = new ScopeImpl("foo");

    //WHEN
    boolean equals = scope.equals(scope2);
    boolean equals2 = scope2.equals(scope);
    int hashScope = scope.hashCode();
    int hashScope2 = scope2.hashCode();

    //THEN
    assertThat(equals, is(true));
    assertThat(equals2, is(true));
    assertThat(hashScope, is(hashScope2));
  }

  @Test
  public void testEqualsAndHashCode_shouldReturnFalse_whenNodeHasDifferentName() {
    //GIVEN
    Scope scope = new ScopeImpl("foo");
    Scope scope2 = new ScopeImpl("bar");

    //WHEN
    boolean equals = scope.equals(scope2);
    int hashScope = scope.hashCode();
    int hashScope2 = scope2.hashCode();

    //THEN
    assertThat(equals, is(false));
    assertThat(hashScope, not(is(hashScope2)));
  }
}