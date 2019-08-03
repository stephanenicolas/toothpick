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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Inject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import toothpick.Toothpick;

public class TestInjectionAndGetInstance {
  @Rule public ToothPickRule toothPickRule = new ToothPickRule(this, "Foo");
  EntryPoint entryPoint;

  @After
  public void tearDown() throws Exception {
    // needs to be performed after test execution
    // not before as rule are initialized before @Before
    Toothpick.reset();
  }

  @Test
  public void testGetInstance() throws Exception {
    // GIVEN
    // WHEN
    entryPoint = toothPickRule.getInstance(EntryPoint.class);
    // THEN
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint.dependency, notNullValue());
  }

  @Test
  public void testInject() throws Exception {
    // GIVEN
    EntryPoint entryPoint = new EntryPoint();

    // WHEN
    toothPickRule.inject(entryPoint);

    // THEN
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint.dependency, notNullValue());
  }

  static class EntryPoint {
    @Inject Dependency dependency;
  }

  static class Dependency {
    @Inject
    Dependency() {}
  }
}
