package toothpick.sample;

import org.easymock.EasyMockRule;
import org.easymock.TestSubject;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import toothpick.ToothPick;
import toothpick.registries.factory.FactoryRegistryLocator;
import toothpick.registries.memberscope.MemberInjectorRegistryLocator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ComputerTest {

  @Rule public EasyMockRule mocks = new EasyMockRule(this);
  @TestSubject private Computer computerUnderTest = ToothPick.openScope("Computer").getInstance(Computer.class);

  @BeforeClass
  public static void setUp() throws Exception {
    MemberInjectorRegistryLocator.addRegistry(new toothpick.sample.MemberInjectorRegistry());
    FactoryRegistryLocator.addRegistry(new toothpick.sample.FactoryRegistry());
  }

  @Test
  public void testMultiply() throws Exception {
    //GIVEN

    //WHEN
    int result = computerUnderTest.compute();

    //THEN
    assertThat(result, is(2));
  }
}