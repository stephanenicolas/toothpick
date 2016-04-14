package toothpick.sample;

import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import toothpick.testing.ToothPickRule;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SimpleEntryPointTestWithRules {

  //do not use @Rule here, we use a chain below
  public ToothPickRule toothPickRule = new ToothPickRule(this, "SimpleEntryPoint")
      //for testing this can be optional.
      //not adding the registry would lead to slightly different code being run compared to production
      //to find factories and member injectors.
      .setRootRegistryPackage("toothpick.sample");

  @Rule public TestRule chain = RuleChain.outerRule(toothPickRule).around(new EasyMockRule(this));

  @Mock private Computer mockComputer;
  @Mock private Computer2 mockComputer2;
  //do not use test subject from easymock, it by passes toothpick injection
  private SimpleEntryPoint simpleEntryPointUnderTest = new SimpleEntryPoint();

  @Test
  public void testMultiply() throws Exception {
    //GIVEN
    expect(mockComputer.compute()).andReturn(4);
    expect(mockComputer2.compute()).andReturn(4);
    replay(mockComputer, mockComputer2);

    toothPickRule.inject(simpleEntryPointUnderTest);

    //WHEN
    int result = simpleEntryPointUnderTest.multiply();

    //THEN
    assertThat(result, is(48));
    verify(mockComputer);
  }
}