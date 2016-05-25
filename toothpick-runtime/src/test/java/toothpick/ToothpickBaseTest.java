package toothpick;

import org.junit.After;
import org.junit.BeforeClass;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

public class ToothpickBaseTest {
  protected ToothpickBaseTest() {
  }

  @BeforeClass
  public static void setUp() throws Exception {
    MemberInjectorRegistryLocator.setRootRegistry(new toothpick.test.MemberInjectorRegistry());
    FactoryRegistryLocator.setRootRegistry(new toothpick.test.FactoryRegistry());
  }

  @After
  public void tearDown() throws Exception {
    Toothpick.reset();
  }
}
