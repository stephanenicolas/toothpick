package toothpick.testing;

import org.junit.runners.model.Statement;
import toothpick.Toothpick;

class ToothPickStatement extends Statement {

  private final Statement base;

  ToothPickStatement(Statement base) {
    this.base = base;
  }

  @Override
  public void evaluate() throws Throwable {
    try {
      base.evaluate();
    } finally {
      Toothpick.reset();
    }
  }
}
