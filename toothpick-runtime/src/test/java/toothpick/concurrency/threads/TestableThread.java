package toothpick.concurrency.threads;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static toothpick.concurrency.TestUtil.log;

public abstract class TestableThread extends Thread {
  protected AtomicBoolean isSuccessful = new AtomicBoolean(false);

  public TestableThread(String name) {
    super(name);
  }

  @Override
  public final void run() {
    log(format("Thread %s starting", getName()));
    try {
      doRun();
    } catch (Exception e) {
      System.err.println(format("Thread %s crashed", getName()));
      e.printStackTrace();
    }
    log(format("Thread %s finished", getName()));
  }

  protected abstract void doRun();

  protected void setIsSuccessful(boolean b) {
    isSuccessful.set(b);
  }

  public boolean isSuccessful() {
    return isSuccessful.get();
  }
}
