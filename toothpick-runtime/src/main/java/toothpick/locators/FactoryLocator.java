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
package toothpick.locators;

import toothpick.Factory;

/**
 * The locator retrieves a {@link Factory} for a given class. In case no generated factory for a
 * given class, we throw a {@link NoFactoryFoundException}.
 *
 * @see Factory
 */
public class FactoryLocator {
  private FactoryLocator() {}

  @SuppressWarnings("unchecked")
  public static <T> Factory<T> getFactory(Class<T> clazz) {
    try {
      Class<? extends Factory<T>> factoryClass =
          (Class<? extends Factory<T>>) Class.forName(clazz.getName() + "__Factory");
      return factoryClass.newInstance();
    } catch (Exception e) {
      throw new NoFactoryFoundException(clazz, e);
    }
  }
}
