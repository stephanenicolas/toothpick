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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static toothpick.concurrency.utils.ThreadTestUtil.STANDARD_THREAD_COUNT;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import toothpick.Toothpick;
import toothpick.ToothpickVisibilityExposer;
import toothpick.concurrency.threads.AddSameScopeThread;
import toothpick.concurrency.threads.AddScopeToListThread;
import toothpick.concurrency.threads.RemoveSameScopeThread;
import toothpick.concurrency.threads.RemoveScopeFromListThread;
import toothpick.concurrency.threads.TestableThread;
import toothpick.concurrency.utils.ThreadTestUtil;

public class ToothpickManipulationsMultiThreadTest {

  static final String ROOT_SCOPE = "ROOT_SCOPE";
  final List<Object> scopeNames = new CopyOnWriteArrayList<>();

  @Before
  public void setUp() throws Exception {
    Toothpick.openScope(ROOT_SCOPE);
    scopeNames.clear();
  }

  @After
  public void tearDown() throws Exception {
    Toothpick.reset();
  }

  @Test
  public void concurrentOpenScopes_shouldNotCrash() throws InterruptedException {
    // GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    // WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      AddScopeToListThread runnable = new AddScopeToListThread(scopeNames);
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
  public void concurrentCloseScopes_shouldNotCrash() throws InterruptedException {
    // GIVEN
    final int scopeCount = 100;
    for (int indexScope = 0; indexScope < scopeCount; indexScope++) {
      Object newScopeName = new Object();
      scopeNames.add(newScopeName);
      Toothpick.openScopes(ROOT_SCOPE, newScopeName);
    }
    final int removalNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    // WHEN
    for (int indexThread = 0; indexThread < removalNodeThreadCount; indexThread++) {
      RemoveScopeFromListThread runnable = new RemoveScopeFromListThread(scopeNames);
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
  public void concurrentOpenAndCloseScopes_shouldNotCrash() throws InterruptedException {
    // GIVEN
    final int removalNodeThreadCount = STANDARD_THREAD_COUNT;
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();
    final Random random = new Random();

    // WHEN
    for (int indexThread = 0;
        indexThread < addNodeThreadCount + removalNodeThreadCount;
        indexThread++) {
      final TestableThread runnable;
      if (random.nextInt(100) < 50) {
        runnable = new RemoveScopeFromListThread(scopeNames);
      } else {
        runnable = new AddScopeToListThread(scopeNames);
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

  @Test
  public void concurrentOpenScopes_shouldAddChildScopeOnlyOnce_withSameChildScope()
      throws InterruptedException {
    // GIVEN
    final int addSameScopeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    // WHEN
    for (int indexThread = 0; indexThread < addSameScopeThreadCount; indexThread++) {
      final TestableThread runnable = new AddSameScopeThread(ROOT_SCOPE, "childScope");
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    // THEN
    ThreadTestUtil.shutdown();
    for (TestableThread thread : threadList) {
      assertTrue(
          String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
    assertThat(ToothpickVisibilityExposer.getScopeNamesSize(), is(2));
  }

  @Test
  public void concurrentOpenScopes_shouldAddChildScopeAtMostOnce_withSameChildScope()
      throws InterruptedException {
    // GIVEN
    final int addSameScopeThreadCount = STANDARD_THREAD_COUNT / 2;
    final int removeSameScopeThreadCount = STANDARD_THREAD_COUNT / 2;
    List<TestableThread> threadList = new ArrayList<>();
    final Random random = new Random();

    // WHEN
    for (int indexThread = 0;
        indexThread < addSameScopeThreadCount + removeSameScopeThreadCount;
        indexThread++) {
      final TestableThread runnable;
      if (random.nextInt(100) < 50) {
        runnable = new RemoveSameScopeThread("childScope");
      } else {
        runnable = new AddSameScopeThread(ROOT_SCOPE, "childScope");
      }
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    // THEN
    ThreadTestUtil.shutdown();
    for (TestableThread thread : threadList) {
      assertTrue(
          String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
    assertThat(ToothpickVisibilityExposer.getScopeNamesSize(), anyOf(is(1), is(2)));
  }
}
