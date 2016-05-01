package toothpick.concurrency.utils;

public class TestUtil {

  public final static boolean DEBUG = false;

  private TestUtil() {
  }

  public static void log(String s) {
    if (DEBUG) {
      System.out.println(s);
    }
  }
}