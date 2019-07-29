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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static toothpick.config.Binding.Mode;

import javax.inject.Provider;

public class BaseBindingTest {

  protected static class StringProvider implements Provider<String> {
    @Override
    public String get() {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  <T> Binding<T> getBinding(Module module) {
    return module.getBindingSet().iterator().next();
  }

  <T> void assertBinding(
      Binding<T> binding,
      Mode mode,
      Class<T> keyClass,
      String name,
      Class<? extends T> implementationClass,
      T instance,
      Provider<? extends T> providerInstance,
      Class<? extends Provider<T>> providerClass,
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
    if (instance == null) {
      assertThat(binding.getInstance(), nullValue());
    } else {
      assertThat(binding.getInstance(), sameInstance(instance));
    }
    if (providerInstance == null) {
      assertThat(binding.getProviderInstance(), nullValue());
    } else {
      assertThat(binding.getProviderInstance(), sameInstance((Object) providerInstance));
    }
    if (providerClass == null) {
      assertThat(binding.getProviderClass(), nullValue());
    } else {
      assertThat(binding.getProviderClass().getName(), is(providerClass.getName()));
    }
    assertThat(binding.isCreatingSingleton(), is(isSingleton));
    assertThat(binding.isCreatingReleasable(), is(isReleasable));
    assertThat(binding.isProvidingSingleton(), is(isProducingSingleton));
    assertThat(binding.isProvidingReleasable(), is(isProducingReleasable));
  }
}
