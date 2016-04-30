package toothpick.concurrency;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import toothpick.ToothPick;

import static org.junit.Assert.assertTrue;
import static toothpick.concurrency.ThreadTestUtil.*;

public class MultiThreadTest {

  static final String ROOT_SCOPE = "ROOT_SCOPE";

  @Before
  public void setUp() throws Exception {
    ToothPick.openScope(ROOT_SCOPE);
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
      AddNodeThread addNodeThread = new AddNodeThread();
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
    final int removalNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    //WHEN
    for (int indexThread = 0; indexThread < removalNodeThreadCount; indexThread++) {
      RemoveNodeThread removeNodeThread = new RemoveNodeThread();
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
}
