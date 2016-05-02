package toothpick.concurrency.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadTestUtil {
  public static final int STANDARD_THREAD_COUNT = 10000;
  static ExecutorService executorService = Executors.newFixedThreadPool(6);

  private ThreadTestUtil() {
  }

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