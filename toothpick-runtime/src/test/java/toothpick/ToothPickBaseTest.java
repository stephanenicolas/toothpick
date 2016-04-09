package toothpick;

import org.junit.BeforeClass;
import toothpick.registries.factory.FactoryRegistryLocator;
import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

public class ToothPickBaseTest {
  protected ToothPickBaseTest() {
  }

  @BeforeClass public static void setUp() throws Exception {
    MemberInjectorRegistryLocator.addRegistry(new toothpick.test.MemberInjectorRegistry());
    FactoryRegistryLocator.addRegistry(new toothpick.test.FactoryRegistry());
  }
}
