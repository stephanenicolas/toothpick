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

import static toothpick.config.Binding.Mode.SIMPLE;

import javax.inject.Named;
import org.junit.Test;

public class SimpleBindingTest extends BaseBindingTest {

  @Test
  public void testSimpleBindingAPI() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding, SIMPLE, String.class, null, null, null, null, null, false, false, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsString() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo");

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding, SIMPLE, String.class, "foo", null, null, null, null, false, false, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsAnnotation() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        SIMPLE,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testSimpleBindingAPI_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding, SIMPLE, String.class, null, null, null, null, null, true, false, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsString_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding, SIMPLE, String.class, "foo", null, null, null, null, true, false, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsAnnotation_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        SIMPLE,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        null,
        null,
        true,
        false,
        false,
        false);
  }

  @Test
  public void testSimpleBindingAPI_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).singleton().releasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding, SIMPLE, String.class, null, null, null, null, null, true, true, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsString_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").singleton().releasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding, SIMPLE, String.class, "foo", null, null, null, null, true, true, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsAnnotation_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).singleton().releasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        SIMPLE,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        null,
        null,
        true,
        true,
        false,
        false);
  }
}
