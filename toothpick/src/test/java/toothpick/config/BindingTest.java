/*
 * Copyright 2016 Stephane Nicolas
 * Copyright 2016 Daniel Molinero Reguerra
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static toothpick.config.Binding.Mode;
import static toothpick.config.Binding.Mode.SIMPLE;

import javax.inject.Named;
import javax.inject.Provider;
import org.junit.Test;

public class BindingTest {

  @Test
  public void testSimpleBindingAPI() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class);

    // THEN
    Binding binding = getBinding(module);
    assertBinding(binding, SIMPLE, String.class, null, null, false, false, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsString() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo");

    // THEN
    Binding binding = getBinding(module);
    assertBinding(binding, SIMPLE, String.class, "foo", null, false, false, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsAnnotation() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class);

    // THEN
    Binding binding = getBinding(module);
    assertBinding(
        binding,
        SIMPLE,
        String.class,
        Named.class.getCanonicalName(),
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
    Binding binding = getBinding(module);
    assertBinding(binding, SIMPLE, String.class, null, null, true, false, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsString_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").singleton();

    // THEN
    Binding binding = getBinding(module);
    assertBinding(binding, SIMPLE, String.class, "foo", null, true, false, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsAnnotation_asSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).singleton();

    // THEN
    Binding binding = getBinding(module);
    assertBinding(
        binding,
        SIMPLE,
        String.class,
        Named.class.getCanonicalName(),
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
    Binding binding = getBinding(module);
    assertBinding(binding, SIMPLE, String.class, null, null, true, true, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsString_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName("foo").singleton().releasable();

    // THEN
    Binding binding = getBinding(module);
    assertBinding(binding, SIMPLE, String.class, "foo", null, true, true, false, false);
  }

  @Test
  public void testSimpleBindingAPI_withNameAsAnnotation_asReleasableSingleton() {
    // GIVEN
    Module module = new Module();

    // WHEN
    module.bind(String.class).withName(Named.class).singleton().releasable();

    // THEN
    Binding binding = getBinding(module);
    assertBinding(
        binding,
        SIMPLE,
        String.class,
        Named.class.getCanonicalName(),
        null,
        true,
        true,
        false,
        false);
  }

  @Test
  public void testBindingAPI() {
    // bind to class
    new Module().bind(String.class).to(String.class);

    // bind to class with name
    new Module().bind(String.class).withName("").to(String.class);

    // bind to class with name and singleton
    new Module().bind(String.class).withName("").to(String.class).singleton();

    // bind to class with name and releasable singleton
    new Module().bind(String.class).withName("").to(String.class).singleton().releasable();

    // bind to class with name and releasable singleton
    new Module().bind(String.class).withName("").to(String.class).singleton().releasable();

    // binding to provider instance
    new Module().bind(String.class).toProviderInstance(new StringProvider());

    // binding to provider instance in scope
    new Module().bind(String.class).toProviderInstance(new StringProvider());

    // binding to provider instance with name
    new Module().bind(String.class).withName("").toProviderInstance(new StringProvider());

    // binding to provider instance and provides singleton
    new Module().bind(String.class).toProviderInstance(new StringProvider()).providesSingleton();

    // binding to provider instance and provides releasable singleton
    new Module()
        .bind(String.class)
        .toProviderInstance(new StringProvider())
        .providesSingleton()
        .providesReleasable();

    // binding to provider and provides singleton and provider singleton
    new Module()
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .singleton();

    // binding to provider and provides singleton and provider releasable singleton
    new Module()
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable()
        .singleton();

    // binding to provider and provider singleton
    new Module().bind(String.class).toProvider(StringProvider.class).singleton();

    // binding to provider and provider releasable singleton
    new Module().bind(String.class).toProvider(StringProvider.class).singleton().releasable();

    // binding to provider and provides singleton
    new Module().bind(String.class).toProvider(StringProvider.class).providesSingleton();

    // binding to provider and provides releasable singleton
    new Module()
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable();

    // binding to provider and provides singleton and provider singleton
    new Module()
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .singleton();

    // binding to provider and provides singleton and provider releasable singleton
    new Module()
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable()
        .singleton();

    // binding to provider and provides singleton and provider releasable singleton
    new Module()
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable()
        .singleton()
        .releasable();
  }

  static class StringProvider implements Provider<String> {
    @Override
    public String get() {
      return null;
    }
  }

  private Binding getBinding(Module module) {
    return module.getBindingSet().iterator().next();
  }

  private void assertBinding(
      Binding binding,
      Mode mode,
      Class<?> keyClass,
      String name,
      Class<?> implementationClass,
      boolean isSingleton,
      boolean isReleasable,
      boolean isProducingSingleton,
      boolean isProducingReleasable) {
    assertThat(binding.getMode(), is(mode));
    if (keyClass == null) {
      assertThat(binding.getKey(), nullValue());
    } else {
      assertThat(binding.getKey().getName(), is(keyClass.getName()));
    }
    if (name == null) {
      assertThat(binding.getName(), nullValue());
    } else {
      assertThat(binding.getName(), is(name));
    }
    if (implementationClass == null) {
      assertThat(binding.getImplementationClass(), nullValue());
    } else {
      assertThat(binding.getImplementationClass().getName(), is(implementationClass.getName()));
    }
    assertThat(binding.isCreatingSingleton(), is(isSingleton));
    assertThat(binding.isCreatingReleasable(), is(isReleasable));
    assertThat(binding.isProvidingSingleton(), is(isProducingSingleton));
    assertThat(binding.isProvidingReleasable(), is(isProducingReleasable));
  }
}
