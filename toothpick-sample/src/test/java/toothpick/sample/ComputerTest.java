package toothpick.sample;

import org.easymock.EasyMockRule;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import toothpick.Toothpick;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ComputerTest {

  @Rule public EasyMockRule mocks = new EasyMockRule(this);
  @TestSubject private Computer computerUnderTest = Toothpick.openScope("Computer").getInstance(Computer.class);

  @BeforeClass
  public static void setUp() throws Exception {
    MemberInjectorRegistryLocator.setRootRegistry(new toothpick.sample.MemberInjectorRegistry());
    FactoryRegistryLocator.setRootRegistry(new toothpick.sample.FactoryRegistry());
  }

  @After
  public void tearDown() throws Exception {
    Toothpick.reset();
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