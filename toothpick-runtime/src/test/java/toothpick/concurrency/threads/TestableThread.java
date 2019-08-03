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

import static java.lang.String.format;
import static toothpick.concurrency.utils.TestUtil.log;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TestableThread implements Runnable {
  protected AtomicBoolean isSuccessful = new AtomicBoolean(false);
  private String name;

  public TestableThread(String name) {
    this.name = name;
  }

  @Override
  public final void run() {
    log(format("Thread %s starting", name));
    try {
      doRun();
    } catch (Exception e) {
      System.err.println(format("Thread %s crashed", name));
      e.printStackTrace();
    }
    log(format("Thread %s finished", name));
  }

  public String getName() {
    return name;
  }

  protected abstract void doRun();

  protected void setIsSuccessful(boolean b) {
    isSuccessful.set(b);
  }

  public boolean isSuccessful() {
    return isSuccessful.get();
  }
}
