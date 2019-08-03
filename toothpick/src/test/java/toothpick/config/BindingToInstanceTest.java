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
package toothpick.config;

import static toothpick.config.Binding.Mode.INSTANCE;

import javax.inject.Named;
import org.junit.Test;

public class BindingToInstanceTest extends BaseBindingTest {

  @Test
  public void testBindingToInstanceAPI() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).toInstance("bar");

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding, INSTANCE, String.class, null, null, "bar", null, null, false, false, false, false);
  }

  @Test
  public void testBindingToInstanceAPI_withNameAsString() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").toInstance("bar");

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        INSTANCE,
        String.class,
        "foo",
        null,
        "bar",
        null,
        null,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToInstanceAPI_withNameAsAnnotation() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).toInstance("bar");

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        INSTANCE,
        String.class,
        Named.class.getCanonicalName(),
        null,
        "bar",
        null,
        null,
        false,
        false,
        false,
        false);
  }
}
