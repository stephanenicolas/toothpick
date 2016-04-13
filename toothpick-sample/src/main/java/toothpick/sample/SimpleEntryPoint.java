package toothpick.sample;

import javax.inject.Inject;
import toothpick.Scope;
import toothpick.ToothPick;

public class SimpleEntryPoint {

  @Inject Computer computer;
  Computer2 computer2;

  @Inject
  public void setComputer2(Computer2 computer2) {
    this.computer2 = computer2;
  }

  public SimpleEntryPoint() {
    Scope scope = ToothPick.openScope("SimpleEntryPoint");
    ToothPick.inject(this, scope);
  }

  public int multiply() {
    return 3 * computer.compute() * computer2.compute();
  }
}
