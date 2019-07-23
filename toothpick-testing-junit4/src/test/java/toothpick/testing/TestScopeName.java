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
package toothpick.testing;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import toothpick.Toothpick;

public class TestScopeName {
  @Rule public ToothPickRule toothPickRule = new ToothPickRule(this, "Foo");
  @Rule public ToothPickRule toothPickRuleWithoutScopeName = new ToothPickRule(this);

  @After
  public void tearDown() throws Exception {
    // needs to be performed after test execution
    // not before as rule are initialized before @Before
    Toothpick.reset();
  }

  @Test(expected = IllegalStateException.class)
  public void testSetScopeName_shouldFail_whenScopeNameWasAlreadySet() throws Exception {
    toothPickRule.setScopeName("Bar");
  }

  @Test(expected = IllegalStateException.class)
  public void testSetScopeName_shouldFail_whenScopeNameAlreadyContainsATestModule()
      throws Exception {
    toothPickRuleWithoutScopeName.setScopeName("Foo");
  }

  @Test
  public void testScopeNameSetByConstruction() throws Exception {
    assertThat(toothPickRule.getScope(), notNullValue());
    assertThat(toothPickRule.getScope().getName(), is((Object) "Foo"));
    assertThat(toothPickRule.getTestModule(), notNullValue());
  }

  @Test
  public void testSetScopeName() throws Exception {
    toothPickRuleWithoutScopeName.setScopeName("Bar");
    assertThat(toothPickRuleWithoutScopeName.getScope(), notNullValue());
    assertThat(toothPickRuleWithoutScopeName.getScope().getName(), is((Object) "Bar"));
    assertThat(toothPickRuleWithoutScopeName.getTestModule(), notNullValue());
  }
}
