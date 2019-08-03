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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsSame.sameInstance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import toothpick.config.Module;

class TestCustomScopeWithNamedBinding {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "Foo");

  private EntryPoint entryPoint;

  @Test
  void testGetInstance_shouldReturnNamedBinding_whenAskingNamedBinding() throws Exception {
    // GIVEN
    ModuleWithNamedBindings moduleWithNamedBindings = new ModuleWithNamedBindings();
    toothPickExtension.getScope().installModules(moduleWithNamedBindings);

    // WHEN
    entryPoint = toothPickExtension.getInstance(EntryPoint.class, "Foo");

    // THEN
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint, sameInstance(moduleWithNamedBindings.instance));
  }

  private static class EntryPoint {}

  private static class ModuleWithNamedBindings extends Module {
    EntryPoint instance = new EntryPoint();

    ModuleWithNamedBindings() {
      bind(EntryPoint.class).withName("Foo").toInstance(instance);
    }
  }
}
