package toothpick.concurrency.utils;

import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import toothpick.Scope;
import toothpick.ToothPick;

public class ThreadTestUtil {
  private static final Random RANDOM = new Random();
  private static final int RANDOM_INTERVAL_LENGTH = 100;
  public static final int STANDARD_THREAD_COUNT = 10000;
  static ExecutorService executorService = Executors.newFixedThreadPool(6);

  private ThreadTestUtil() {
  }

  public static void submit(Runnable runnable) {
    executorService.submit(runnable);
  }

  public static void shutdown() {
    try {
      executorService.shutdown();
      executorService.awaitTermination(STANDARD_THREAD_COUNT / 100, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      executorService = Executors.newFixedThreadPool(6);
    }
  }

  public static Scope findRandomNode(Object rooScopeName, int acceptanceThreshold) {
    Scope root = ToothPick.openScope(rooScopeName);
    Scope result = null;
    Stack<Scope> scopeStack = new Stack<Scope>();
    scopeStack.push(root);
    while (result == null && !scopeStack.isEmpty()) {
      Scope scope = scopeStack.pop();
      if (RANDOM.nextInt(RANDOM_INTERVAL_LENGTH) < acceptanceThreshold && scope != root) {
        result = scope;
      } else {
        for (Scope childScope : scope.getChildrenScopes()) {
          scopeStack.push(childScope);
        }
      }
    }
    return result;
  }

  public static Scope findRandomNode(List<Object> scopeNames, int acceptanceThreshold) {
    if (RANDOM.nextInt(RANDOM_INTERVAL_LENGTH) < acceptanceThreshold) {
      return null;
    } else {
      synchronized (scopeNames) {
        if (scopeNames.isEmpty()) {
          return null;
        }
        int position = RANDOM.nextInt(scopeNames.size());
        return ToothPick.openScope(scopeNames.get(position));
      }
    }
  }
}