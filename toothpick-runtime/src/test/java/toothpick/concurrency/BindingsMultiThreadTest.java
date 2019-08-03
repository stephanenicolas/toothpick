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
import static org.powermock.api.mockito.PowerMockito.when;
import static toothpick.concurrency.utils.ThreadTestUtil.STANDARD_THREAD_COUNT;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import toothpick.Factory;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.concurrency.threads.GetInstanceThread;
import toothpick.concurrency.threads.InstallBindingThread;
import toothpick.concurrency.threads.ScopeToStringThread;
import toothpick.concurrency.threads.TestableThread;
import toothpick.concurrency.utils.ClassCreator;
import toothpick.concurrency.utils.ThreadTestUtil;
import toothpick.configuration.Configuration;
import toothpick.locators.FactoryLocator;

@PrepareForTest(FactoryLocator.class)
public class BindingsMultiThreadTest {

  public @Rule PowerMockRule rule = new PowerMockRule();

  private static final String ROOT_SCOPE = "ROOT_SCOPE";
  private final List<Object> scopeNames = new CopyOnWriteArrayList<>();
  private static ClassCreator classCreator = new ClassCreator();
  private ArgumentCaptor<Class> argument;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    PowerMockito.mockStatic(FactoryLocator.class);
    argument = ArgumentCaptor.forClass(Class.class);
    when(FactoryLocator.getFactory(argument.capture()))
        .thenAnswer(
            new Answer<Factory>() {
              @Override
              public Factory answer(InvocationOnMock invocationOnMock) {
                return new DynamicTestClassesFactory<>(argument.getValue(), true);
              }
            });
    Toothpick.setConfiguration(Configuration.forProduction());
    Toothpick.openScope(ROOT_SCOPE);
    scopeNames.clear();
  }

  @After
  @SuppressWarnings("unchecked")
  public void tearDown() {
    Toothpick.reset();
    ThreadTestUtil.shutdown();
    try {
      PowerMockito.verifyStatic(FactoryLocator.class, Mockito.atLeast(0));
      for (Class allValue : argument.getAllValues()) {
        FactoryLocator.getFactory(allValue);
      }
    } catch (Exception e) {
      // ignored
    }
  }

  @Test
  public void concurrentBindingInstall_shouldNotCrash() {
    // GIVEN
    List<TestableThread> threadList = new ArrayList<>();

    // WHEN
    for (int indexThread = 0; indexThread < STANDARD_THREAD_COUNT; indexThread++) {
      InstallBindingThread runnable = new InstallBindingThread(classCreator, ROOT_SCOPE);
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
  public void concurrentBindingInstallAndToString_shouldNotCrash() {
    // GIVEN
    List<TestableThread> threadList = new ArrayList<>();
    Random random = new Random();

    // WHEN
    for (int indexThread = 0; indexThread < STANDARD_THREAD_COUNT; indexThread++) {
      TestableThread runnable;
      if (random.nextInt(100) < 20) {
        runnable = new InstallBindingThread(classCreator, ROOT_SCOPE);
      } else {
        runnable = new ScopeToStringThread(ROOT_SCOPE);
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
  public void concurrentBindingInstallAndGetInstance_shouldNotCrash() {
    // GIVEN
    List<TestableThread> threadList = new ArrayList<>();
    Random random = new Random();

    // WHEN
    for (int indexThread = 0; indexThread < STANDARD_THREAD_COUNT; indexThread++) {
      TestableThread runnable;
      if (random.nextInt(100) < 20) {
        runnable = new InstallBindingThread(classCreator, ROOT_SCOPE);
      } else {
        runnable =
            new GetInstanceThread(
                ROOT_SCOPE,
                classCreator.allClasses[random.nextInt(classCreator.allClasses.length)]);
      }
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    // THEN
    // we simply should not have crashed when all threads are done
    boolean timeout = ThreadTestUtil.shutdown();
    assertTrue("Executor service should not timeout.", timeout);
    for (TestableThread thread : threadList) {
      assertTrue(
          String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentScopedGetInstance_shouldNotCrash() {
    // GIVEN
    List<TestableThread> threadList = new ArrayList<>();
    Random random = new Random();

    // WHEN
    for (int indexThread = 0; indexThread < STANDARD_THREAD_COUNT; indexThread++) {
      TestableThread runnable =
          new GetInstanceThread(
              ROOT_SCOPE, classCreator.allClasses[random.nextInt(classCreator.allClasses.length)]);
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    // THEN
    // we simply should not have crashed when all threads are done
    boolean timeout = ThreadTestUtil.shutdown();
    assertTrue("Executor service should not timeout.", timeout);
    for (TestableThread thread : threadList) {
      assertTrue(
          String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
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
    public boolean hasSingletonAnnotation() {
      return false;
    }

    @Override
    public boolean hasReleasableAnnotation() {
      return false;
    }

    @Override
    public boolean hasProvidesSingletonAnnotation() {
      return false;
    }

    @Override
    public boolean hasProvidesReleasableAnnotation() {
      return false;
    }
  }
}
