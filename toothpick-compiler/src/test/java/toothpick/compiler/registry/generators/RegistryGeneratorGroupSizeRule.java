package toothpick.compiler.registry.generators;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public final class RegistryGeneratorGroupSizeRule implements TestRule {

  private final int groupSizeForTests;

  public RegistryGeneratorGroupSizeRule(int groupSizeForTests) {
    this.groupSizeForTests = groupSizeForTests;
  }

  @Override
  public Statement apply(final Statement base, Description description) {
    final int defaultGroupSize = RegistryGenerator.groupSize;

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          RegistryGenerator.groupSize = groupSizeForTests;
          base.evaluate();
        } finally {
          RegistryGenerator.groupSize = defaultGroupSize;
        }
      }
    };
  }

}
