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

import toothpick.MemberInjector;

/**
 * Locates the {@link MemberInjector} instances. If not {@link MemberInjector} is found, we simply
 * return {@code null}. This is required to fully support polymorphism when injecting dependencies.
 *
 * @see MemberInjector
 */
public class MemberInjectorLocator {
  private MemberInjectorLocator() {}

  @SuppressWarnings("unchecked")
  public static <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    try {
      Class<? extends MemberInjector<T>> memberInjectorClass =
          (Class<? extends MemberInjector<T>>) Class.forName(clazz.getName() + "__MemberInjector");
      return memberInjectorClass.newInstance();
    } catch (Exception e) {
      return null;
    }
  }
}
