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

import static toothpick.config.Binding.Mode.PROVIDER_INSTANCE;

import javax.inject.Named;
import org.junit.Test;

public class BindingToProviderInstanceTest extends BaseBindingTest {

  @Test
  public void testBindingToProviderInstanceAPI() {
    // GIVEN
    Module module = new Module();
    StringProvider providerInstance = new StringProvider();

    // WHEN
    module.bind(String.class).toProviderInstance(providerInstance);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_INSTANCE,
        String.class,
        null,
        null,
        null,
        providerInstance,
        null,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToProviderInstanceAPI_withNameAsString() {
    // GIVEN
    Module module = new Module();
    StringProvider providerInstance = new StringProvider();

    // WHEN
    module.bind(String.class).withName("foo").toProviderInstance(providerInstance);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_INSTANCE,
        String.class,
        "foo",
        null,
        null,
        providerInstance,
        null,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToProviderInstanceAPI_withNameAsAnnotation() {
    // GIVEN
    Module module = new Module();
    StringProvider providerInstance = new StringProvider();

    // WHEN
    module.bind(String.class).withName(Named.class).toProviderInstance(providerInstance);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_INSTANCE,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        providerInstance,
        null,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToProviderInstanceAPI_asProvidesSingleton() {
    // GIVEN
    Module module = new Module();
    StringProvider providerInstance = new StringProvider();

    // WHEN
    module.bind(String.class).toProviderInstance(providerInstance).providesSingleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_INSTANCE,
        String.class,
        null,
        null,
        null,
        providerInstance,
        null,
        false,
        false,
        true,
        false);
  }

  @Test
  public void testBindingToProviderInstanceAPI_withNameAsString_asProvidesSingleton() {
    // GIVEN
    Module module = new Module();
    StringProvider providerInstance = new StringProvider();

    // WHEN
    module
        .bind(String.class)
        .withName("foo")
        .toProviderInstance(providerInstance)
        .providesSingleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_INSTANCE,
        String.class,
        "foo",
        null,
        null,
        providerInstance,
        null,
        false,
        false,
        true,
        false);
  }

  @Test
  public void testBindingToProviderInstanceAPI_withNameAsAnnotation_asProvidesSingleton() {
    // GIVEN
    Module module = new Module();
    StringProvider providerInstance = new StringProvider();

    // WHEN
    module
        .bind(String.class)
        .withName(Named.class)
        .toProviderInstance(providerInstance)
        .providesSingleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_INSTANCE,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        providerInstance,
        null,
        false,
        false,
        true,
        false);
  }

  @Test
  public void testBindingToProviderInstanceAPI_asProvidesReleasableSingleton() {
    // GIVEN
    Module module = new Module();
    StringProvider providerInstance = new StringProvider();

    // WHEN
    module
        .bind(String.class)
        .toProviderInstance(providerInstance)
        .providesSingleton()
        .providesReleasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_INSTANCE,
        String.class,
        null,
        null,
        null,
        providerInstance,
        null,
        false,
        false,
        true,
        true);
  }

  @Test
  public void testBindingToProviderInstanceAPI_withNameAsString_asProvidesReleasableSingleton() {
    // GIVEN
    Module module = new Module();
    StringProvider providerInstance = new StringProvider();

    // WHEN
    module
        .bind(String.class)
        .withName("foo")
        .toProviderInstance(providerInstance)
        .providesSingleton()
        .providesReleasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_INSTANCE,
        String.class,
        "foo",
        null,
        null,
        providerInstance,
        null,
        false,
        false,
        true,
        true);
  }

  @Test
  public void
      testBindingToProviderInstanceAPI_withNameAsAnnotation_asProvidesReleasableSingleton() {
    // GIVEN
    Module module = new Module();
    StringProvider providerInstance = new StringProvider();

    // WHEN
    module
        .bind(String.class)
        .withName(Named.class)
        .toProviderInstance(providerInstance)
        .providesSingleton()
        .providesReleasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_INSTANCE,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        providerInstance,
        null,
        false,
        false,
        true,
        true);
  }
}
