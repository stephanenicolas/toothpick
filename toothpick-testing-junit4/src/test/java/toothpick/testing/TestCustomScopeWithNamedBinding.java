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
import static org.hamcrest.core.IsSame.sameInstance;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import toothpick.Toothpick;
import toothpick.config.Module;

public class TestCustomScopeWithNamedBinding {
  @Rule public ToothPickRule toothPickRule = new ToothPickRule(this, "Foo");
  EntryPoint entryPoint;

  @After
  public void tearDown() throws Exception {
    // needs to be performed after test execution
    // not before as rule are initialized before @Before
    Toothpick.reset();
  }

  @Test
  public void testGetInstance_shouldReturnNamedBinding_whenAskingNamedBinding() throws Exception {
    // GIVEN
    ModuleWithNamedBindings moduleWithNamedBindings = new ModuleWithNamedBindings();
    toothPickRule.getScope().installModules(moduleWithNamedBindings);

    // WHEN
    entryPoint = toothPickRule.getInstance(EntryPoint.class, "Foo");

    // THEN
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint, sameInstance(moduleWithNamedBindings.instance));
  }

  public static class EntryPoint {}

  public static class ModuleWithNamedBindings extends Module {
    public EntryPoint instance = new EntryPoint();

    public ModuleWithNamedBindings() {
      bind(EntryPoint.class).withName("Foo").toInstance(instance);
    }
  }
}
