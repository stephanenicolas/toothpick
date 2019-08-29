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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Test;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.data.Foo;

public class DisableScopesTest {

  @Test(expected = RuntimeException.class)
  public void openScope_shouldFail_whenScopesAreDisabled() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment().disableScopes());

    // WHEN
    Toothpick.openScope(this);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = RuntimeException.class)
  public void inject_shouldFail_whenScopesAreNotDisabled() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment());

    // WHEN
    Toothpick.inject(this);

    // THEN
    fail("Should throw an exception");
  }

  @Test(expected = RuntimeException.class)
  public void installModules_shouldFail_whenScopesAreNotDisabled() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment());

    // WHEN
    Toothpick.installModules(new TestModule());

    // THEN
    fail("Should throw an exception");
  }

  @Test()
  public void inject_shouldUseDefaultScope_whenScopesAreDisabled() {
    // GIVEN
    Toothpick.setConfiguration(Configuration.forDevelopment().disableScopes());

    Foo foo = new Foo();
    // WHEN
    Toothpick.inject(foo);

    // THEN
    assertThat(foo.bar, notNullValue());
  }

  @After
  public void tearDown() {
    Toothpick.reset();
  }

  private static class TestModule extends Module {
    public TestModule() {
      // do some bindings here
    }
  }
}
