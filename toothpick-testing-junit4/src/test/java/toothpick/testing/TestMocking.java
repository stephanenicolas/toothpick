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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.inject.Inject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import toothpick.Toothpick;

public class TestMocking {
  @Rule(order = 1)
  public ToothPickRule toothPickRule = new ToothPickRule(this, "Foo");

  @Rule(order = 2)
  public MockitoRule mockitoJUnitRule = MockitoJUnit.rule();

  EntryPoint entryPoint;
  @Mock Dependency dependency;

  @After
  public void tearDown() throws Exception {
    // needs to be performed after test execution
    // not before as rule are initialized before @Before
    Toothpick.reset();
  }

  @Test
  public void testMock() throws Exception {
    // GIVEN
    when(dependency.num()).thenReturn(2);
    // WHEN
    entryPoint = toothPickRule.getInstance(EntryPoint.class);
    int num = entryPoint.dependency.num();
    // THEN
    verify(dependency).num();
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint.dependency, notNullValue());
    assertThat(num, is(2));
  }

  public static class EntryPoint {
    @Inject Dependency dependency;
  }

  public static class Dependency {
    public int num() {
      return 1;
    }
  }
}
