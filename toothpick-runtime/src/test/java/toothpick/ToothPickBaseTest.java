package toothpick;

import org.junit.BeforeClass;
import toothpick.integration.data.FactoryRegistry;
import toothpick.integration.data.MemberInjectorRegistry;
import toothpick.registries.factory.FactoryRegistryLocator;
import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

public class ToothPickBaseTest {
  protected ToothPickBaseTest() {
  }

  @BeforeClass public static void setUp() throws Exception {
    MemberInjectorRegistryLocator.addRegistry(new MemberInjectorRegistry());
    FactoryRegistryLocator.addRegistry(new FactoryRegistry());
  }
}
