package toothpick.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import toothpick.ToothPick;
import toothpick.concurrency.threads.AddNodeThread;
import toothpick.concurrency.threads.AddScopeToListThread;
import toothpick.concurrency.threads.RemoveNodeThread;
import toothpick.concurrency.threads.RemoveScopeFromListThread;
import toothpick.concurrency.threads.TestableThread;

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
  public void concurrentScopeAdditions_shouldNotCrash() throws InterruptedException {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      AddScopeToListThread addNodeThread = new AddScopeToListThread(scopeNames);
      threadList.add(addNodeThread);
      addNodeThread.start();
    }

    //THEN
    //we simply should not have crashed when all threads are done
    for (TestableThread thread : threadList) {
      thread.join();
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentScopeRemovals_shouldNotCrash() throws InterruptedException {
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
      RemoveScopeFromListThread removeNodeThread = new RemoveScopeFromListThread(scopeNames);
      threadList.add(removeNodeThread);
      removeNodeThread.start();
    }

    //THEN
    //we simply should not have crashed when all threads are done
    for (TestableThread thread : threadList) {
      thread.join();
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentScopeAdditionsAndRemovals_shouldNotCrash() throws InterruptedException {
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
}
