package toothpick.sample;

import javax.inject.Inject;

public class Computer {

  @Inject public Computer() {
  }

  public int compute() {
    return 2;
  }
}
