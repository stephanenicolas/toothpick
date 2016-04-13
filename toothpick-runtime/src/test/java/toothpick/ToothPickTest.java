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

public class ToothPickTest extends ToothPickBaseTest {

  @Test
  public void getScope_shouldNotReturnNull_whenNoScopeByThisKeyWasCreated() throws Exception {
    //GIVEN

    //WHEN
    Scope scope = ToothPick.openScope(this);

    //THEN
    assertThat(scope, notNullValue());
  }

  @Test
  public void getScope_shouldReturnAnScope_whenThisScopeByThisKeyWasCreated() throws Exception {
    //GIVEN
    Scope scope = ToothPick.openScope(this);

    //WHEN
    Scope scope2 = ToothPick.openScope(this);

    //THEN
    assertThat(scope, notNullValue());
    assertThat(scope, sameInstance(scope2));
  }

  @Test
  public void createScope_shouldReturnAnScopeWithAParent_whenThisScopeByThisKeyWasCreatedWithAParent() throws Exception {
    //GIVEN
    Scope scopeParent = ToothPick.openScope("foo");

    //WHEN
    Scope scope = ToothPick.openScope("bar");
    scopeParent.addChild(scope);

    //THEN
    assertThat(scope, notNullValue());
    assertThat(scope.getParentScope(), sameInstance(scopeParent));
  }

  @Test
  public void reset_shouldClear_WhenSomeScopesWereCreated() throws Exception {
    //GIVEN
    Scope scope0 = ToothPick.openScope("foo");
    Scope scope1 = ToothPick.openScope("bar");

    //WHEN
    ToothPick.reset();
    Scope scope0AfterReset = ToothPick.openScope("foo");
    Scope scope1AfterReset = ToothPick.openScope("bar");

    //THEN
    assertThat(scope0AfterReset, not(sameInstance(scope0)));
    assertThat(scope1AfterReset, not(sameInstance(scope1)));
  }

  @Test
  public void destroyScope_shouldClearThisScope_WhenThisScopesWasCreated() throws Exception {
    //GIVEN
    Scope scope = ToothPick.openScope("foo");

    //WHEN
    ToothPick.closeScope("foo");
    Scope scopeAfterReset = ToothPick.openScope("foo");

    //THEN
    assertThat(scopeAfterReset, not(sameInstance(scope)));
  }

  @Test
  public void getOrCreateScope_shouldReturnSameScope_WhenOneWasCreatedWithSameKey() throws Exception {
    //GIVEN
    Scope scope = ToothPick.openScope("foo");

    //WHEN
    Scope scope2 = ToothPick.openScope("foo");

    //THEN
    assertThat(scope, sameInstance(scope2));
  }

  @Test
  public void getOrCreateScope_shouldReturnANewScopeScope_WhenOneWasNotCreatedWithSameKey() throws Exception {
    //GIVEN
    Scope scopeParent = ToothPick.openScope("bar");

    //WHEN
    Scope scope = ToothPick.openScope("foo");
    scopeParent.addChild(scope);
    Scope scope2 = ToothPick.openScope("foo");

    //THEN
    assertThat(scope, notNullValue());
    assertThat(scope2, sameInstance(scope));
    assertThat(scope.getParentScope(), sameInstance(scopeParent));
  }

  @Test
  public void destroyScope_shouldNotFail_WhenThisScopesWasNotCreated() throws Exception {
    //GIVEN

    //WHEN
    ToothPick.closeScope("foo");

    //THEN
  }

  @Test
  public void constructor_shouldBePrivate() throws Exception {
    //GIVEN

    //WHEN
    Constructor constructor = ToothPick.class.getDeclaredConstructor();

    //THEN
    assertThat(constructor, notNullValue());
    assertThat(constructor.isAccessible(), is(false));
  }

  @Test(expected = InvocationTargetException.class)
  public void constructor_shouldThrowException_whenCalled() throws Exception {
    //GIVEN

    //WHEN
    Constructor constructor = ToothPick.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();

    //THEN
    fail("default constructor should not be invokable even via reflection");
  }

  @After
  public void tearDown() throws Exception {
    ToothPick.reset();
  }
}