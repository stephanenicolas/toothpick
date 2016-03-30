package toothpick.sample;

import javax.inject.Inject;
import toothpick.Injector;
import toothpick.ToothPick;

public class SimpleEntryPoint {

  @Inject Computer computer;

  public SimpleEntryPoint() {
    Injector injector = ToothPick.getOrCreateInjector(null, "SimpleEntryPoint");
    injector.inject(this);
  }

  public int multiply() {
    return 3 * computer.compute();
  }
}
