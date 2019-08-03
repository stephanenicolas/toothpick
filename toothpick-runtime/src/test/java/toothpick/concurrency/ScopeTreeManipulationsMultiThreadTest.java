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
package toothpick.concurrency;

import static org.junit.Assert.assertTrue;
import static toothpick.concurrency.utils.ThreadTestUtil.STANDARD_THREAD_COUNT;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import toothpick.Toothpick;
import toothpick.concurrency.threads.AddNodeThread;
import toothpick.concurrency.threads.RemoveNodeThread;
import toothpick.concurrency.threads.TestableThread;
import toothpick.concurrency.utils.ThreadTestUtil;

public class ScopeTreeManipulationsMultiThreadTest {

  static final String ROOT_SCOPE = "ROOT_SCOPE";

  @Before
  public void setUp() throws Exception {
    Toothpick.openScope(ROOT_SCOPE);
  }

  @After
  public void tearDown() throws Exception {
    Toothpick.reset();
  }

  @Test
  public void concurrentScopeAdditions_shouldNotCrash() throws InterruptedException {
    // GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    // WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      AddNodeThread runnable = new AddNodeThread(ROOT_SCOPE);
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    // THEN
    // we simply should not have crashed when all threads are done
    ThreadTestUtil.shutdown();
    for (TestableThread thread : threadList) {
      assertTrue(
          String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentScopeRemovals_shouldNotCrash() throws InterruptedException {
    // GIVEN
    final int removalNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    // WHEN
    for (int indexThread = 0; indexThread < removalNodeThreadCount; indexThread++) {
      RemoveNodeThread runnable = new RemoveNodeThread(ROOT_SCOPE);
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    // THEN
    // we simply should not have crashed when all threads are done
    ThreadTestUtil.shutdown();
    for (TestableThread thread : threadList) {
      assertTrue(
          String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentScopeAdditionsAndRemovals_shouldNotCrash() throws InterruptedException {
    // GIVEN
    final int removalNodeThreadCount = STANDARD_THREAD_COUNT / 2;
    final int addNodeThreadCount = STANDARD_THREAD_COUNT / 2;
    List<TestableThread> threadList = new ArrayList<>();
    final Random random = new Random();

    // WHEN
    for (int indexThread = 0;
        indexThread < addNodeThreadCount + removalNodeThreadCount;
        indexThread++) {
      final TestableThread runnable;
      if (random.nextInt(100) < 50) {
        runnable = new RemoveNodeThread(ROOT_SCOPE);
      } else {
        runnable = new AddNodeThread(ROOT_SCOPE);
      }
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    // THEN
    // we simply should not have crashed when all threads are done
    ThreadTestUtil.shutdown();
    for (TestableThread thread : threadList) {
      assertTrue(
          String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }
}
