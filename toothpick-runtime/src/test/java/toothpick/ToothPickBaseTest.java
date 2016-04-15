package toothpick;

import org.junit.After;
import org.junit.BeforeClass;
import toothpick.registries.factory.FactoryRegistryLocator;
import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

public class ToothPickBaseTest {
  protected ToothPickBaseTest() {
  }

  @BeforeClass
  public static void setUp() throws Exception {
    MemberInjectorRegistryLocator.setRootRegistry(new toothpick.test.MemberInjectorRegistry());
    FactoryRegistryLocator.setRootRegistry(new toothpick.test.FactoryRegistry());
  }

  @After
  public void tearDown() throws Exception {
    ToothPick.reset();
  }
}
