package toothpick;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ToothpickTest extends ToothpickBaseTest2 {

  @Test
  public void getScope_shouldNotReturnNull_whenNoScopeByThisKeyWasCreated() throws Exception {
    //GIVEN

    //WHEN
    Scope scope = Toothpick.openScope(this);

    //THEN
    assertThat(scope, notNullValue());
  }

  @Test
  public void getScope_shouldReturnAnScope_whenThisScopeByThisKeyWasCreated() throws Exception {
    //GIVEN
    Scope scope = Toothpick.openScope(this);

    //WHEN
    Scope scope2 = Toothpick.openScope(this);

    //THEN
    assertThat(scope, notNullValue());
    assertThat(scope, sameInstance(scope2));
  }

  @Test
  public void createScope_shouldReturnAnScopeWithAParent_whenThisScopeByThisKeyWasCreatedWithAParent() throws Exception {
    //GIVEN
    ScopeNode scopeParent = (ScopeNode) Toothpick.openScope("foo");

    //WHEN
    ScopeNode scope = (ScopeNode) Toothpick.openScope("bar");
    scopeParent.addChild(scope);

    //THEN
    assertThat(scope, notNullValue());
    assertThat(scope.getParentScope(), sameInstance(scopeParent));
  }

  @Test(expected = IllegalArgumentException.class)
  public void openScopes_shouldFail_whenScopeNamesAreNull() throws Exception {
    //GIVEN

    //WHEN
    Toothpick.openScopes((Object[]) null);

    //THEN
    fail("Shoudl ahve thrown an exception");
  }

  @Test
  public void reset_shouldClear_WhenSomeScopesWereCreated() throws Exception {
    //GIVEN
    Scope scope0 = Toothpick.openScope("foo");
    Scope scope1 = Toothpick.openScope("bar");

    //WHEN
    Toothpick.reset();
    Scope scope0AfterReset = Toothpick.openScope("foo");
    Scope scope1AfterReset = Toothpick.openScope("bar");

    //THEN
    assertThat(scope0AfterReset, not(sameInstance(scope0)));
    assertThat(scope1AfterReset, not(sameInstance(scope1)));
  }

  @Test
  public void destroyScope_shouldClearThisScope_WhenThisScopesWasCreated() throws Exception {
    //GIVEN
    Scope scope = Toothpick.openScope("foo");

    //WHEN
    Toothpick.closeScope("foo");
    Scope scopeAfterReset = Toothpick.openScope("foo");

    //THEN
    assertThat(scopeAfterReset, not(sameInstance(scope)));
  }

  @Test
  public void getOrCreateScope_shouldReturnSameScope_WhenOneWasCreatedWithSameKey() throws Exception {
    //GIVEN
    Scope scope = Toothpick.openScope("foo");

    //WHEN
    Scope scope2 = Toothpick.openScope("foo");

    //THEN
    assertThat(scope, sameInstance(scope2));
  }

  @Test
  public void getOrCreateScope_shouldReturnANewScopeScope_WhenOneWasNotCreatedWithSameKey() throws Exception {
    //GIVEN
    ScopeNode scopeParent = (ScopeNode) Toothpick.openScope("bar");

    //WHEN
    ScopeNode scope = (ScopeNode) Toothpick.openScope("foo");
    scopeParent.addChild(scope);
    ScopeNode scope2 = (ScopeNode) Toothpick.openScope("foo");

    //THEN
    assertThat(scope, notNullValue());
    assertThat(scope2, sameInstance(scope));
    assertThat(scope.getParentScope(), sameInstance(scopeParent));
  }

  @Test
  public void closeScope_shouldNotFail_WhenThisScopesWasNotCreated() throws Exception {
    //GIVEN

    //WHEN
    Toothpick.closeScope("foo");

    //THEN
  }

  @Test
  public void closeScope_shouldRemoveChildScope_whenChildScopeIsClosed() throws Exception {
    //GIVEN
    Toothpick.openScopes("foo", "bar");

    //WHEN
    Toothpick.closeScope("bar");

    //THEN
    assertThat(((ScopeNode) Toothpick.openScope("foo")).getChildrenScopes().isEmpty(), is(true));
  }

  @Test
  public void constructor_shouldBePrivate() throws Exception {
    //GIVEN

    //WHEN
    Constructor constructor = Toothpick.class.getDeclaredConstructor();

    //THEN
    assertThat(constructor, notNullValue());
    assertThat(constructor.isAccessible(), is(false));
  }

  @Test(expected = InvocationTargetException.class)
  public void constructor_shouldThrowException_whenCalled() throws Exception {
    //GIVEN

    //WHEN
    Constructor constructor = Toothpick.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();

    //THEN
    fail("default constructor should not be invokable even via reflection");
  }

  @After
  public void tearDown() throws Exception {
    Toothpick.reset();
  }
}