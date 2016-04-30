package toothpick.concurrency.threads;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static toothpick.concurrency.utils.TestUtil.log;

public abstract class TestableThread implements Runnable {
  protected AtomicBoolean isSuccessful = new AtomicBoolean(false);
  private String name;

  public TestableThread(String name) {
    this.name = name;
  }

  @Override
  public final void run() {
    log(format("Thread %s starting", name));
    try {
      doRun();
    } catch (Exception e) {
      System.err.println(format("Thread %s crashed", name));
      e.printStackTrace();
    }
    log(format("Thread %s finished", name));
  }

  public String getName() {
    return name;
  }

  protected abstract void doRun();

  protected void setIsSuccessful(boolean b) {
    isSuccessful.set(b);
  }

  public boolean isSuccessful() {
    return isSuccessful.get();
  }
}
