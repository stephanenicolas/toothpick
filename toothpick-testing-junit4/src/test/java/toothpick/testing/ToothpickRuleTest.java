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
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.JUnitCore;

public class ToothpickRuleTest {

  @Test
  public void testRuleIsIntroducedAndEvaluated() {
    SimpleTest.wasRun = false;
    JUnitCore.runClasses(SimpleTest.class);
    assertThat(SimpleTest.wasRun, is(true));
  }

  @Test
  public void testScopeName() {
    assertThat(JUnitCore.runClasses(TestScopeName.class).wasSuccessful(), is(true));
  }

  @Test
  public void testInjectAndGetInstance() {
    assertThat(JUnitCore.runClasses(TestInjectionAndGetInstance.class).wasSuccessful(), is(true));
  }

  @Test
  public void testMock() {
    assertThat(JUnitCore.runClasses(TestMocking.class).wasSuccessful(), is(true));
  }
}
