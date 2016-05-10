package toothpick.testing;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ToothPickRuleTest {

  @Test
  public void testRuleIsIntroducedAndEvaluated() {
    SimpleTest.wasRun = false;
    JUnitCore.runClasses(SimpleTest.class);
    assertThat(SimpleTest.wasRun, is(true));
  }

  @Test
  public void testScopeName() {
    assertThat(JUnitCore.runClasses(TestScopeName.class).wasSuccessful(), is(true));
  }

  @Test
  public void testInjectAndGetInstance() {
    assertThat(JUnitCore.runClasses(TestInjectionAndGetInstance.class).wasSuccessful(), is(true));
  }

  @Test
  public void testMock() {
    assertThat(JUnitCore.runClasses(TestMocking.class).wasSuccessful(), is(true));
  }

  @Test
  public void testBadRegistry() {
    assertThat(JUnitCore.runClasses(TestBadRegistry.class).wasSuccessful(), is(true));
  }
}
