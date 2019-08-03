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

import static toothpick.config.Binding.Mode.CLASS;

import javax.inject.Named;
import org.junit.Test;

public class BindingToClassTest extends BaseBindingTest {

  @Test
  public void testBindingToClassAPI() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).to(String.class);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        CLASS,
        String.class,
        null,
        String.class,
        null,
        null,
        null,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToClassAPI_withNameAsString() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").to(String.class);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        CLASS,
        String.class,
        "foo",
        String.class,
        null,
        null,
        null,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToClassAPI_withNameAsAnnotation() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).to(String.class);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        CLASS,
        String.class,
        Named.class.getCanonicalName(),
        String.class,
        null,
        null,
        null,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToClassAPI_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).to(String.class).singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        CLASS,
        String.class,
        null,
        String.class,
        null,
        null,
        null,
        true,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToClassAPI_withNameAsString_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").to(String.class).singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        CLASS,
        String.class,
        "foo",
        String.class,
        null,
        null,
        null,
        true,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToClassAPI_withNameAsAnnotation_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).to(String.class).singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        CLASS,
        String.class,
        Named.class.getCanonicalName(),
        String.class,
        null,
        null,
        null,
        true,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToClassAPI_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).to(String.class).singleton().releasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        CLASS,
        String.class,
        null,
        String.class,
        null,
        null,
        null,
        true,
        true,
        false,
        false);
  }

  @Test
  public void testBindingToClassAPI_withNameAsString_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").to(String.class).singleton().releasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        CLASS,
        String.class,
        "foo",
        String.class,
        null,
        null,
        null,
        true,
        true,
        false,
        false);
  }

  @Test
  public void testBindingToClassAPI_withNameAsAnnotation_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).to(String.class).singleton().releasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        CLASS,
        String.class,
        Named.class.getCanonicalName(),
        String.class,
        null,
        null,
        null,
        true,
        true,
        false,
        false);
  }
}
