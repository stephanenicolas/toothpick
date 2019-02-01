package toothpick.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import toothpick.configuration.Configuration;
import toothpick.Factory;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.concurrency.threads.GetInstanceThread;
import toothpick.concurrency.threads.InstallBindingThread;
import toothpick.concurrency.threads.ScopeToStringThread;
import toothpick.concurrency.threads.TestableThread;
import toothpick.concurrency.utils.ClassCreator;
import toothpick.concurrency.utils.ThreadTestUtil;
import toothpick.locators.FactoryLocator;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static toothpick.concurrency.utils.ThreadTestUtil.STANDARD_THREAD_COUNT;

@PrepareForTest(FactoryLocator.class)
public class BindingsMultiThreadTest {

  public @Rule PowerMockRule rule = new PowerMockRule();

  static final String ROOT_SCOPE = "ROOT_SCOPE";
  final List<Object> scopeNames = new CopyOnWriteArrayList<>();
  private static ClassCreator classCreator = new ClassCreator();

  @Before
  public void setUp() {

    PowerMock.mockStatic(FactoryLocator.class);
    final Capture<Class> capturedClass = Capture.newInstance();
    expect(FactoryLocator.getFactory(capture(capturedClass))).andAnswer(new IAnswer<Factory>() {
      @Override public Factory answer() throws Throwable {
        return new DynamicTestClassesFactory(capturedClass.getValue(), true);
      }
    }).anyTimes();
    PowerMock.replay(FactoryLocator.class);
    Toothpick.setConfiguration(Configuration.forProduction());
    Toothpick.openScope(ROOT_SCOPE);
    scopeNames.clear();
  }

  @After
  public void tearDown() {
    Toothpick.reset();
    ThreadTestUtil.shutdown();
    PowerMock.verify(FactoryLocator.class);
  }

  @Test
  public void concurrentBindingInstall_shouldNotCrash() {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      InstallBindingThread runnable = new InstallBindingThread(classCreator, ROOT_SCOPE);
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    //THEN
    //we simply should not have crashed when all threads are done
    ThreadTestUtil.shutdown();
    for (TestableThread thread : threadList) {
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentBindingInstallAndToString_shouldNotCrash() {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();
    Random random = new Random();

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      TestableThread runnable;
      if (random.nextInt(100) < 20) {
        runnable = new InstallBindingThread(classCreator, ROOT_SCOPE);
      } else {
        runnable = new ScopeToStringThread(ROOT_SCOPE);
      }
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    //THEN
    //we simply should not have crashed when all threads are done
    ThreadTestUtil.shutdown();
    for (TestableThread thread : threadList) {
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentBindingInstallAndGetInstance_shouldNotCrash() {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();
    Random random = new Random();

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      TestableThread runnable;
      if (random.nextInt(100) < 20) {
        runnable = new InstallBindingThread(classCreator, ROOT_SCOPE);
      } else {
        runnable = new GetInstanceThread(ROOT_SCOPE, classCreator.allClasses[random.nextInt(classCreator.allClasses.length)]);
      }
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    //THEN
    //we simply should not have crashed when all threads are done
    boolean timeout = ThreadTestUtil.shutdown();
    assertTrue("Executor service should not timeout.", timeout);
    for (TestableThread thread : threadList) {
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentScopedGetInstance_shouldNotCrash() {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();
    Random random = new Random();

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      TestableThread runnable = new GetInstanceThread(ROOT_SCOPE, classCreator.allClasses[random.nextInt(classCreator.allClasses.length)]);
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    //THEN
    //we simply should not have crashed when all threads are done
    boolean timeout = ThreadTestUtil.shutdown();
    assertTrue("Executor service should not timeout.", timeout);
    for (TestableThread thread : threadList) {
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  private static class DynamicTestClassesFactory<T> implements Factory<T> {
    private final Class<T> clazz;
    private boolean scoped;

    DynamicTestClassesFactory(Class<T> clazz, boolean scoped) {
      this.clazz = clazz;
      this.scoped = scoped;
    }

    @Override
    public T createInstance(Scope scope) {
      try {
        return clazz.newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Scope getTargetScope(Scope currentScope) {
      return currentScope;
    }

    @Override
    public boolean hasScopeAnnotation() {
      return scoped;
    }

    @Override
    public boolean hasProvidesSingletonInScopeAnnotation() {
      return false;
    }
  }
}
