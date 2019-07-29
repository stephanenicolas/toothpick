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
package toothpick.concurrency.threads;

import toothpick.Toothpick;

public class GetInstanceThread extends TestableThread {
  static int instanceNumber = 0;
  private Object scopeName;
  private Class<?> clazz;

  public GetInstanceThread(Object scopeName, Class<?> clazz) {
    super("GetInstanceThread " + instanceNumber++);
    this.scopeName = scopeName;
    this.clazz = clazz;
  }

  @Override
  public void doRun() {
    Toothpick.openScope(scopeName).getInstance(clazz);
    setIsSuccessful(true);
  }
}
