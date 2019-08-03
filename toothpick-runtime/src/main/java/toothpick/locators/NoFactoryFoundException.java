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

import static java.lang.String.format;

public class NoFactoryFoundException extends RuntimeException {
  public NoFactoryFoundException(Class clazz) {
    this(clazz, null);
  }

  public NoFactoryFoundException(Class clazz, Throwable cause) {
    super(
        format(
            "No factory could be found for class %s. " //
                + "Check that the class has either a @Inject annotated constructor " //
                + "or contains @Inject annotated members.",
            clazz.getName()),
        cause);
  }
}
