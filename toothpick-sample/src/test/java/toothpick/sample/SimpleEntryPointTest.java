package toothpick.sample;

import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Rule;
import org.junit.Test;
import toothpick.Injector;
import toothpick.ToothPick;
import toothpick.config.Module;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SimpleEntryPointTest {

  @Rule public EasyMockRule mocks = new EasyMockRule(this);
  @TestSubject private SimpleEntryPoint simpleEntryPointUnderTest = new SimpleEntryPoint();
  @Mock private Computer mockComputer;

  @Test public void testMultiply() throws Exception {
    //GIVEN
    expect(mockComputer.compute()).andReturn(4);
    replay(mockComputer);

    final Injector injector = ToothPick.getOrCreateInjector(null, "SimpleEntryPoint");
    injector.installOverrideModules(new TestModule());

    //WHEN
    int result = simpleEntryPointUnderTest.multiply();

    //THEN
    assertThat(result, is(12));
    verify(mockComputer);
  }

  private class TestModule extends Module {
    public TestModule() {
      bind(Computer.class).to(mockComputer);
    }
  }
}