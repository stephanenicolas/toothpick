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
package toothpick.concurrency.utils;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadTestUtil {
  private static final Random RANDOM = new Random();
  private static final int RANDOM_INTERVAL_LENGTH = 100;
  public static final int STANDARD_THREAD_COUNT = 10000;
  static ExecutorService executorService = Executors.newFixedThreadPool(6);

  private ThreadTestUtil() {}

  public static void submit(Runnable runnable) {
    executorService.submit(runnable);
  }

  public static boolean shutdown() {
    try {
      executorService.shutdown();
      return executorService.awaitTermination(STANDARD_THREAD_COUNT / 100, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      return true;
    } finally {
      executorService = Executors.newFixedThreadPool(6);
    }
  }
}
