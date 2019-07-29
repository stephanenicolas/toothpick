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

import static toothpick.config.Binding.Mode.PROVIDER_CLASS;

import javax.inject.Named;
import org.junit.Test;

public class BindingToProviderClassTest extends BaseBindingTest {

  @Test
  public void testBindingToProviderClassAPI() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).toProvider(StringProvider.class);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        null,
        null,
        null,
        null,
        StringProvider.class,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsString() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").toProvider(StringProvider.class);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        "foo",
        null,
        null,
        null,
        StringProvider.class,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsAnnotation() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).toProvider(StringProvider.class);

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        null,
        StringProvider.class,
        false,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).toProvider(StringProvider.class).singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        null,
        null,
        null,
        null,
        StringProvider.class,
        true,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsString_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").toProvider(StringProvider.class).singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        "foo",
        null,
        null,
        null,
        StringProvider.class,
        true,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsAnnotation_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).toProvider(StringProvider.class).singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        null,
        StringProvider.class,
        true,
        false,
        false,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).toProvider(StringProvider.class).singleton().releasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        null,
        null,
        null,
        null,
        StringProvider.class,
        true,
        true,
        false,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsString_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module
        .bind(String.class)
        .withName("foo")
        .toProvider(StringProvider.class)
        .singleton()
        .releasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        "foo",
        null,
        null,
        null,
        StringProvider.class,
        true,
        true,
        false,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsAnnotation_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module
        .bind(String.class)
        .withName(Named.class)
        .toProvider(StringProvider.class)
        .singleton()
        .releasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        null,
        StringProvider.class,
        true,
        true,
        false,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_asProvidesSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).toProvider(StringProvider.class).providesSingleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        null,
        null,
        null,
        null,
        StringProvider.class,
        false,
        false,
        true,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsString_asProvidesSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").toProvider(StringProvider.class).providesSingleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        "foo",
        null,
        null,
        null,
        StringProvider.class,
        false,
        false,
        true,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsAnnotation_asProvidesSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module
        .bind(String.class)
        .withName(Named.class)
        .toProvider(StringProvider.class)
        .providesSingleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        null,
        StringProvider.class,
        false,
        false,
        true,
        false);
  }

  @Test
  public void testBindingToProviderClassAPI_asProviesReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        null,
        null,
        null,
        null,
        StringProvider.class,
        false,
        false,
        true,
        true);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsString_asProvidesReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module
        .bind(String.class)
        .withName("foo")
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        "foo",
        null,
        null,
        null,
        StringProvider.class,
        false,
        false,
        true,
        true);
  }

  @Test
  public void testBindingToProviderClassAPI_withNameAsAnnotation_asProvidesReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module
        .bind(String.class)
        .withName(Named.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        null,
        StringProvider.class,
        false,
        false,
        true,
        true);
  }

  @Test
  public void testBindingToProviderClassAPI_asProvidesReleasableSingletonAndSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable()
        .singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        null,
        null,
        null,
        null,
        StringProvider.class,
        true,
        false,
        true,
        true);
  }

  @Test
  public void
      testBindingToProviderClassAPI_withNameAsString_asProvidesReleasableSingletonAndSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module
        .bind(String.class)
        .withName("foo")
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable()
        .singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        "foo",
        null,
        null,
        null,
        StringProvider.class,
        true,
        false,
        true,
        true);
  }

  @Test
  public void
      testBindingToProviderClassAPI_withNameAsAnnotation_asProvidesReleasableSingletonAndSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module
        .bind(String.class)
        .withName(Named.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable()
        .singleton();

    // THEN
    Binding<String> binding = getBinding(module);
    assertBinding(
        binding,
        PROVIDER_CLASS,
        String.class,
        Named.class.getCanonicalName(),
        null,
        null,
        null,
        StringProvider.class,
        true,
        false,
        true,
        true);
  }
}
