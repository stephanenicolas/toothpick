package toothpick.sample;

import javax.inject.Inject;
import toothpick.Injector;
import toothpick.ToothPick;

public class SimpleEntryPoint {

  @Inject Computer computer;
  Computer2 computer2;

  @Inject
  public void setComputer2(Computer2 computer2) {
    this.computer2 = computer2;
  }

  public SimpleEntryPoint() {
    Injector injector = ToothPick.openInjector("SimpleEntryPoint");
    injector.inject(this);
  }

  public int multiply() {
    return 3 * computer.compute() * computer2.compute();
  }
}
