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

import javax.inject.Named;
import javax.inject.Provider;
import org.junit.Test;

public class BindingTest {

  @Test
  public void testBindingAPI() {

    /*
      BIND TO INSTANCE
    */

    // bind to instance
    new Module().bind(String.class).toInstance("");

    // bind to instance with name
    new Module().bind(String.class).withName("").toInstance("");
    new Module().bind(String.class).withName(Named.class).toInstance("");

    /*
      BIND TO
    */

    // bind to class
    new Module().bind(String.class).to(String.class);

    // bind to class with name
    new Module().bind(String.class).withName("").to(String.class);
    new Module().bind(String.class).withName(Named.class).to(String.class);

    // bind to class and singleton
    new Module().bind(String.class).to(String.class).singleton();

    // bind to class and releasable singleton
    new Module().bind(String.class).to(String.class).singleton().releasable();

    /*
      BIND TO PROVIDER INSTANCE
    */

    // bind to provider instance
    new Module().bind(String.class).toProviderInstance(new StringProvider());

    // bind to provider instance with name
    new Module().bind(String.class).withName("").toProviderInstance(new StringProvider());
    new Module().bind(String.class).withName(Named.class).toProviderInstance(new StringProvider());

    // bind to provider instance and provides singleton
    new Module().bind(String.class).toProviderInstance(new StringProvider()).providesSingleton();

    // bind to provider instance and provides releasable singleton
    new Module()
        .bind(String.class)
        .toProviderInstance(new StringProvider())
        .providesSingleton()
        .providesReleasable();

    /*
      BIND TO PROVIDER
    */

    // bind to provider
    new Module().bind(String.class).toProvider(StringProvider.class);

    // bind to provider with name
    new Module().bind(String.class).withName("").toProvider(StringProvider.class);
    new Module().bind(String.class).withName(Named.class).toProvider(StringProvider.class);

    // bind to provider and provider singleton
    new Module().bind(String.class).toProvider(StringProvider.class).singleton();

    // bind to provider and provider releasable singleton
    new Module().bind(String.class).toProvider(StringProvider.class).singleton().releasable();

    // bind to provider and provides singleton
    new Module().bind(String.class).toProvider(StringProvider.class).providesSingleton();

    // bind to provider and provides releasable singleton
    new Module()
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable();

    // bind to provider and provides releasable singleton and provider singleton
    new Module()
        .bind(String.class)
        .toProvider(StringProvider.class)
        .providesSingleton()
        .providesReleasable()
        .singleton();
  }

  static class StringProvider implements Provider<String> {
    @Override
    public String get() {
      return null;
    }
  }
}
