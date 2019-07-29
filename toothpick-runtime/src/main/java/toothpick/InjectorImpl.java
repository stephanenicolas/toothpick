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
package toothpick;

import toothpick.locators.MemberInjectorLocator;

/** Default implementation of an injector. */
public class InjectorImpl implements Injector {
  /**
   * {@inheritDoc}
   *
   * <p>We will bubble up the hierarchy, starting at class {@code T}. As soon as a {@link
   * MemberInjector} is found, we use it to inject the members of {@code obj}.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> void inject(T obj, Scope scope) {
    Class<? super T> currentClass = (Class<T>) obj.getClass();
    do {
      MemberInjector<? super T> memberInjector =
          MemberInjectorLocator.getMemberInjector(currentClass);
      if (memberInjector != null) {
        memberInjector.inject(obj, scope);
        return;
      } else {
        currentClass = currentClass.getSuperclass();
      }
    } while (currentClass != null);
  }
}
