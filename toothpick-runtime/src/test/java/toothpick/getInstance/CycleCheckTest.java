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
package toothpick.getInstance;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.configuration.CyclicDependencyException;
import toothpick.data.CyclicFoo;
import toothpick.data.CyclicNamedFoo;
import toothpick.data.IFoo;
import toothpick.locators.NoFactoryFoundException;

/*
 * Creates a instance in the simplest possible way
 * without any module.
 */
public class CycleCheckTest {

  @BeforeClass
  public static void setUp() {
    Toothpick.setConfiguration(Configuration.forDevelopment());
  }

  @AfterClass
  public static void staticTearDown() {
    Toothpick.setConfiguration(Configuration.forProduction());
  }

  @After
  public void tearDown() {
    Toothpick.reset();
  }

  @Test(expected = CyclicDependencyException.class)
  public void testSimpleCycleDetection() {
    // GIVEN
    Scope scope = new ScopeImpl("");

    // WHEN
    scope.getInstance(CyclicFoo.class);

    // THEN
    fail("Should throw an exception as a cycle is detected");
  }

  @Test
  public void testCycleDetection_whenSameClass_and_differentName_shouldNotCrash() {
    // GIVEN
    final CyclicNamedFoo instance1 = new CyclicNamedFoo();
    Scope scope = new ScopeImpl("");
    scope.installModules(
        new Module() {
          {
            bind(CyclicNamedFoo.class).withName("foo").toInstance(instance1);
          }
        });

    // WHEN
    CyclicNamedFoo instance2 = scope.getInstance(CyclicNamedFoo.class);

    // THEN
    // Should not crashed
    assertThat(instance2, notNullValue());
    assertThat(instance2.cyclicFoo, sameInstance(instance1));
  }

  @Test(expected = NoFactoryFoundException.class)
  public void testCycleDetection_whenGetInstanceFails_shouldCloseCycle() {
    // GIVEN
    Scope scope = new ScopeImpl("");

    // WHEN
    try {
      scope.getInstance(IFoo.class);
    } catch (NoFactoryFoundException nfe) {
      nfe.printStackTrace();
    }

    scope.getInstance(IFoo.class);

    // THEN
    fail(
        "Should throw NoFactoryFoundException as IFoo does not have any implementation bound."
            + "But It should not throw CyclicDependencyException as it was removed from the stack.");
  }
}
