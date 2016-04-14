package toothpick.sample;

import org.junit.Rule;
import org.junit.Test;
import toothpick.testing.ToothPickRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ComputerTestWithRules {

  @Rule public ToothPickRule toothPickRule = new ToothPickRule(this, "").setRootRegistryPackage("toothpick.sample");

  private Computer computerUnderTest = toothPickRule.getInstance(Computer.class);

  @Test
  public void testMultiply() throws Exception {
    //GIVEN

    //WHEN
    int result = computerUnderTest.compute();

    //THEN
    assertThat(result, is(2));
  }
}