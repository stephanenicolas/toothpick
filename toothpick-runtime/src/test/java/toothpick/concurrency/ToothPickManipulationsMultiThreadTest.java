package toothpick.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import toothpick.ToothPick;
import toothpick.concurrency.threads.AddSameScopeThread;
import toothpick.concurrency.threads.AddScopeToListThread;
import toothpick.concurrency.threads.RemoveSameScopeThread;
import toothpick.concurrency.threads.RemoveScopeFromListThread;
import toothpick.concurrency.threads.TestableThread;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static toothpick.concurrency.utils.ThreadTestUtil.STANDARD_THREAD_COUNT;

public class ToothPickManipulationsMultiThreadTest {

  static final String ROOT_SCOPE = "ROOT_SCOPE";
  final List<Object> scopeNames = new CopyOnWriteArrayList<>();

  @Before
  public void setUp() throws Exception {
    ToothPick.openScope(ROOT_SCOPE);
    scopeNames.clear();
  }

  @After
  public void tearDown() throws Exception {
    ToothPick.reset();
  }

  @Test
  public void concurrentOpenScopes_shouldNotCrash() throws InterruptedException {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      AddScopeToListThread thread = new AddScopeToListThread(scopeNames);
      threadList.add(thread);
      thread.start();
    }

    //THEN
    //we simply should not have crashed when all threads are done
    for (TestableThread thread : threadList) {
      thread.join();
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentCloseScopes_shouldNotCrash() throws InterruptedException {
    //GIVEN
    final int scopeCount = 100;
    for (int indexScope = 0; indexScope < scopeCount; indexScope++) {
      Object newScopeName = new Object();
      scopeNames.add(newScopeName);
      ToothPick.openScopes(ROOT_SCOPE, newScopeName);
    }
    final int removalNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    //WHEN
    for (int indexThread = 0; indexThread < removalNodeThreadCount; indexThread++) {
      RemoveScopeFromListThread thread = new RemoveScopeFromListThread(scopeNames);
      threadList.add(thread);
      thread.start();
    }

    //THEN
    //we simply should not have crashed when all threads are done
    for (TestableThread thread : threadList) {
      thread.join();
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentOpenAndCloseScopes_shouldNotCrash() throws InterruptedException {
    //GIVEN
    final int removalNodeThreadCount = STANDARD_THREAD_COUNT;
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();
    final Random random = new Random();

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount + removalNodeThreadCount; indexThread++) {
      final TestableThread testableThread;
      if (random.nextInt(100) < 50) {
        testableThread = new RemoveScopeFromListThread(scopeNames);
      } else {
        testableThread = new AddScopeToListThread(scopeNames);
      }
      threadList.add(testableThread);
      testableThread.start();
    }

    //THEN
    //we simply should not have crashed when all threads are done
    for (TestableThread thread : threadList) {
      thread.join();
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentOpenScopes_shouldAddChildScopeOnlyOnce_withSameChildScope() throws InterruptedException {
    //GIVEN
    final int addSameScopeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    //WHEN
    for (int indexThread = 0; indexThread < addSameScopeThreadCount; indexThread++) {
      final TestableThread testableThread = new AddSameScopeThread(ROOT_SCOPE, "childScope");
      threadList.add(testableThread);
      testableThread.start();
    }

    //THEN
    for (TestableThread thread : threadList) {
      thread.join();
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
    assertThat(ToothPick.getScopeNames().size(), is(2));
  }

  @Test
  public void concurrentOpenScopes_shouldAddChildScopeAtMostOnce_withSameChildScope() throws InterruptedException {
    //GIVEN
    final int addSameScopeThreadCount = STANDARD_THREAD_COUNT;
    final int removeSameScopeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();
    final Random random = new Random();

    //WHEN
    for (int indexThread = 0; indexThread < addSameScopeThreadCount + removeSameScopeThreadCount; indexThread++) {
      final TestableThread testableThread;
      if (random.nextInt(100) < 50) {
        testableThread = new RemoveSameScopeThread("childScope");
      } else {
        testableThread = new AddSameScopeThread(ROOT_SCOPE, "childScope");
      }
      threadList.add(testableThread);
      testableThread.start();
    }

    //THEN
    for (TestableThread thread : threadList) {
      thread.join();
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
    assertThat(ToothPick.getScopeNames().size(), anyOf(is(1), is(2)));
  }
}
