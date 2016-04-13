package toothpick.sample;

import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.config.Module;
import toothpick.registries.factory.FactoryRegistryLocator;
import toothpick.registries.memberscope.MemberInjectorRegistryLocator;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SimpleEntryPointTest {

  @Rule public EasyMockRule mocks = new EasyMockRule(this);
  @TestSubject private SimpleEntryPoint simpleEntryPointUnderTest = new SimpleEntryPoint();
  @Mock private Computer mockComputer;
  @Mock private Computer2 mockComputer2;

  @BeforeClass
  public static void setUp() throws Exception {
    MemberInjectorRegistryLocator.addRegistry(new toothpick.sample.MemberInjectorRegistry());
    FactoryRegistryLocator.addRegistry(new toothpick.sample.FactoryRegistry());
  }

  @Test
  public void testMultiply() throws Exception {
    //GIVEN
    expect(mockComputer.compute()).andReturn(4);
    expect(mockComputer2.compute()).andReturn(4);
    replay(mockComputer, mockComputer2);

    final Scope scope = ToothPick.openScope("SimpleEntryPoint");
    scope.installTestModules(new TestModule());

    //WHEN
    int result = simpleEntryPointUnderTest.multiply();

    //THEN
    assertThat(result, is(48));
    verify(mockComputer);
  }

  private class TestModule extends Module {
    public TestModule() {
      bind(Computer.class).to(mockComputer);
    }
  }
}