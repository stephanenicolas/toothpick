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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToObject;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import toothpick.configuration.Configuration;
import toothpick.configuration.MultipleRootException;

public class ToothpickTest {

  @Test
  public void getScope_shouldNotReturnNull_whenNoScopeByThisKeyWasCreated() {
    // GIVEN

    // WHEN
    Scope scope = Toothpick.openScope(this);

    // THEN
    assertThat(scope, notNullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getScope_shouldFail_whenScopeNameIsNull() {
    // GIVEN

    // WHEN
    Toothpick.openScope(null);

    // THEN
    fail("Should throw an exception");
  }

  @Test
  public void getScope_shouldReturnAScope_whenThisScopeByThisKeyWasCreated() {
    // GIVEN
    Scope scope = Toothpick.openScope(this);

    // WHEN
    Scope scope2 = Toothpick.openScope(this);

    // THEN
    assertThat(scope, notNullValue());
    assertThat(scope, sameInstance(scope2));
  }

  @Test
  public void
      createScope_shouldReturnAScopeWithAParent_whenThisScopeByThisKeyWasCreatedWithAParent() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forProduction());
    ScopeNode scopeParent = (ScopeNode) Toothpick.openScope("foo");

    // WHEN
    ScopeNode scope = (ScopeNode) Toothpick.openScope("bar");
    scopeParent.addChild(scope);

    // THEN
    assertThat(scope, notNullValue());
    assertThat(scope.getParentScope(), sameInstance(scopeParent));
  }

  @Test
  public void createScope_shouldMarkThisScopeAsOpen() {
    // GIVEN

    // WHEN
    ScopeImpl scope = (ScopeImpl) Toothpick.openScope("foo");

    // THEN
    assertThat(scope.isOpen, is(true));
  }

  @Test
  public void openScope_shouldApplyScopeConfig_whenNewScopeCreated() {
    // GIVEN
    TestScopeConfig scopeConfig = new TestScopeConfig();
    ScopeImpl scope = (ScopeImpl) Toothpick.openScope("foo", scopeConfig);

    // WHEN
    Toothpick.closeScope("foo");

    // THEN
    assertThat(scope.isOpen, is(false));
    assertThat(scopeConfig.wasApplied, is(true));
  }

  @Test
  public void openScope_shouldNotApplyScopeConfig_whenNoNewScopeCreated() {
    // GIVEN
    TestScopeConfig scopeConfig = new TestScopeConfig();
    Toothpick.openScope("foo");
    ScopeImpl scope = (ScopeImpl) Toothpick.openScope("foo", scopeConfig);

    // WHEN
    Toothpick.closeScope("foo");

    // THEN
    assertThat(scope.isOpen, is(false));
    assertThat(scopeConfig.wasApplied, is(false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void openScopes_shouldFail_whenScopeNamesAreNull() {
    // GIVEN

    // WHEN
    Toothpick.openScopes((Object[]) null);

    // THEN
  }

  @Test(expected = IllegalArgumentException.class)
  public void openScopes_shouldFail_whenScopeNamesAreEmpty() {
    // GIVEN

    // WHEN
    Toothpick.openScopes(new Object[0]);

    // THEN
  }

  @Test
  public void reset_shouldClear_WhenSomeScopesWereCreated() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forProduction());
    Scope scope0 = Toothpick.openScope("foo");
    Scope scope1 = Toothpick.openScope("bar");

    // WHEN
    Toothpick.reset();
    Scope scope0AfterReset = Toothpick.openScope("foo");
    Scope scope1AfterReset = Toothpick.openScope("bar");

    // THEN
    assertThat(scope0AfterReset, not(sameInstance(scope0)));
    assertThat(scope1AfterReset, not(sameInstance(scope1)));
  }

  @Test
  public void destroyScope_shouldClearThisScope_WhenThisScopesWasCreated() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forProduction());
    Scope scope = Toothpick.openScope("foo");

    // WHEN
    Toothpick.closeScope("foo");
    Scope scopeAfterReset = Toothpick.openScope("foo");

    // THEN
    assertThat(scopeAfterReset, not(sameInstance(scope)));
  }

  @Test
  public void closeScope_shouldMarkThisScopeAsClosed() {
    // GIVEN
    ScopeImpl scope = (ScopeImpl) Toothpick.openScope("foo");

    // WHEN
    Toothpick.closeScope("foo");

    // THEN
    assertThat(scope.isOpen, is(false));
  }

  @Test(expected = MultipleRootException.class)
  public void
      openingAClosedChildScope_shouldThrowAnException_whenConfigurationPreventsMultipleRootScopes() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes());
    Toothpick.openScopes("foo", "bar");
    Toothpick.closeScope("bar");

    // WHEN
    Toothpick.openScope("bar");

    // THEN
  }

  @Test(expected = MultipleRootException.class)
  public void
      opening2rootScope_shouldThrowAnException_whenConfigurationPreventsMultipleRootScopes() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes());
    Toothpick.openScope("foo");

    // WHEN
    Toothpick.openScope("bar");

    // THEN
  }

  @Test(expected = MultipleRootException.class)
  public void opening2rootScope_shouldPass_whenSameRootScope() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes());
    Toothpick.openScope("foo");

    // WHEN
    Toothpick.openScope("bar");

    // THEN
  }

  @Test
  public void opening2rootScope_shouldPass_whenConfigurationDoesNotPreventsMultipleRootScopes() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment());
    Toothpick.openScope("foo");

    // WHEN
    Toothpick.openScope("bar");

    // THEN
  }

  @Test
  public void getOrCreateScope_shouldReturnSameScope_WhenOneWasCreatedWithSameKey() {
    // GIVEN
    Scope scope = Toothpick.openScope("foo");

    // WHEN
    Scope scope2 = Toothpick.openScope("foo");

    // THEN
    assertThat(scope, sameInstance(scope2));
  }

  @Test
  public void getOrCreateScope_shouldReturnANewScopeScope_WhenOneWasNotCreatedWithSameKey() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forProduction());
    ScopeNode scopeParent = (ScopeNode) Toothpick.openScope("bar");

    // WHEN
    ScopeNode scope = (ScopeNode) Toothpick.openScope("foo");
    scopeParent.addChild(scope);
    ScopeNode scope2 = (ScopeNode) Toothpick.openScope("foo");

    // THEN
    assertThat(scope, notNullValue());
    assertThat(scope2, sameInstance(scope));
    assertThat(scope.getParentScope(), sameInstance(scopeParent));
  }

  @Test
  public void closeScope_shouldNotFail_WhenThisScopesWasNotCreated() {
    // GIVEN

    // WHEN
    Toothpick.closeScope("foo");

    // THEN
  }

  @Test
  public void closeScope_shouldRemoveChildScope_whenChildScopeIsClosed() {
    // GIVEN
    Toothpick.openScopes("foo", "bar");

    // WHEN
    Toothpick.closeScope("bar");

    // THEN
    assertThat(((ScopeNode) Toothpick.openScope("foo")).getChildrenScopes().isEmpty(), is(true));
  }

  @Test
  public void constructor_shouldBePrivate() throws Exception {
    // GIVEN

    // WHEN
    Constructor constructor = Toothpick.class.getDeclaredConstructor();

    // THEN
    assertThat(constructor, notNullValue());
    assertThat(constructor.isAccessible(), is(false));
  }

  @Test(expected = InvocationTargetException.class)
  public void constructor_shouldThrowException_whenCalled() throws Exception {
    // GIVEN

    // WHEN
    Constructor constructor = Toothpick.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();

    // THEN
  }

  @Test
  public void reset_shouldCallResetForProvidedScope() {
    // GIVEN
    ScopeNode mockScope = mock(ScopeNode.class);
    mockScope.reset();

    // WHEN
    Toothpick.reset(mockScope);

    // THEN
    verify(mockScope);
  }

  @Test(expected = IllegalArgumentException.class)
  public void isScopeOpen_shouldThrowException_WhenNameIsNull() {
    // GIVEN

    // WHEN
    Toothpick.isScopeOpen(null);

    // THEN
    fail("Should throw an exception");
  }

  @Test
  public void isScopeOpen_shouldReturnFalse_WhenThisScopesWasNotCreated() {
    // GIVEN

    // WHEN
    boolean isFooScopeOpen = Toothpick.isScopeOpen("foo");

    // THEN
    assertThat(isFooScopeOpen, is(false));
  }

  @Test
  public void isScopeOpen_shouldReturnTrue_WhenThisScopesWasCreated() {
    // GIVEN
    Toothpick.openScope("foo");

    // WHEN
    boolean isFooScopeOpen = Toothpick.isScopeOpen("foo");

    // THEN
    assertThat(isFooScopeOpen, is(true));
  }

  @Test
  public void isScopeOpen_shouldReturnFalse_WhenThisScopesWasClosed() {
    // GIVEN
    Toothpick.openScope("foo");

    // WHEN
    Toothpick.closeScope("foo");
    boolean isFooScopeOpen = Toothpick.isScopeOpen("foo");

    // THEN
    assertThat(isFooScopeOpen, is(false));
  }

  @Test
  public void isScopeOpen_shouldReturnFalse_WhenParentScopesWasClosed() {
    // GIVEN
    Toothpick.openScopes("foo", "bar");

    // WHEN
    Toothpick.closeScope("foo");
    boolean isBarScopeOpen = Toothpick.isScopeOpen("bar");

    // THEN
    assertThat(isBarScopeOpen, is(false));
  }

  @Test
  public void release_shouldCallScopeRelease() {
    // GIVEN
    Scope scope = mock(ScopeNode.class);
    doNothing().when(scope).release();

    // WHEN
    Toothpick.release(scope);

    // THEN
    verify(scope, Mockito.atLeastOnce()).release();
  }

  @Test
  public void openRootScope_shouldReturnTheRootScope_WhenRootScopeIsDefined() {
    // GIVEN
    Toothpick.openScopes("foo", "bar");

    // WHEN
    Scope rootScope = Toothpick.openRootScope();

    // THEN
    assertThat(rootScope, notNullValue());
    assertThat(rootScope.getName(), equalToObject("foo"));
  }

  @Test
  public void openRootScope_shouldReturnDefaultScope_WhenThereIsNoScopeTreeDefined() {
    // GIVEN

    // WHEN
    Scope rootScope = Toothpick.openRootScope();

    // THEN
    assertThat(rootScope, notNullValue());
    assertThat(rootScope.getName(), equalToObject(Toothpick.class));
  }

  @Test
  public void openRootScope_shouldApplyConfig() {
    // GIVEN
    TestScopeConfig scopeConfig = new TestScopeConfig();

    // WHEN
    Scope rootScope = Toothpick.openRootScope(scopeConfig);

    // THEN
    assertThat(rootScope, notNullValue());
    assertThat(rootScope.getName(), equalToObject(Toothpick.class));
    assertThat(scopeConfig.wasApplied, is(true));
  }

  @Test(expected = RuntimeException.class)
  public void openRootScope_shouldThrowException_WhenThereAreMultipleScopeTreesDefined() {
    // GIVEN
    Toothpick.openScope("foo");
    Toothpick.openScope("bar");

    // WHEN
    Toothpick.openRootScope();

    // THEN
    fail("Should throw an exception for multiple scope trees!");
  }

  @After
  public void tearDown() {
    Toothpick.reset();
  }

  private static class TestScopeConfig implements Scope.ScopeConfig {
    private boolean wasApplied = false;

    @Override
    public void configure(Scope scope) {
      wasApplied = true;
    }

    public boolean wasApplied() {
      return wasApplied;
    }
  }
}
