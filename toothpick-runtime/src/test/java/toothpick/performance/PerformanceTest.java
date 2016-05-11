package toothpick.performance;

import javax.inject.Inject;
import org.junit.Test;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.concurrency.utils.ClassCreator;
import toothpick.concurrency.utils.DynamicTestClassesFactoryRegistry;
import toothpick.registries.FactoryRegistryLocator;

public class PerformanceTest {

  private static final int TOTAL_INJECTION_COUNT = 5 * 1000 * 1000;

  @Test
  public void perform5millionsInjectionsOn1000Classes() {
    //GIVEN
    FactoryRegistryLocator.setRootRegistry(new DynamicTestClassesFactoryRegistry(false));
    ClassCreator classCreator = new ClassCreator();
    Scope rootScope = ToothPick.openScope("root");
    long start = System.currentTimeMillis();
    //WHEN
    for (int times = 0; times < TOTAL_INJECTION_COUNT / classCreator.allClasses.length; times++) {
      for (Class allClass : classCreator.allClasses) {
        rootScope.getInstance(allClass);
      }
    }

    //THEN
    long end = System.currentTimeMillis();
    System.out.printf("%d injections performed in %d ms%n", TOTAL_INJECTION_COUNT, (end - start));
  }
}
