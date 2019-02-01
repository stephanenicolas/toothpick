package toothpick.sample;

import javax.inject.Inject;
import org.easymock.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import toothpick.testing.ToothPickExtension;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SimpleEntryPointTest {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "MyScope");
  @RegisterExtension EasyMockExtension easyMockExtension = new EasyMockExtension(this);

  @Mock private Computer mockComputer;
  @Mock private Computer2 mockComputer2;

  @Inject SimpleEntryPoint simpleEntryPointUnderTest;

  @Test
  public void testMultiply() throws Exception {
    //GIVEN
    expect(mockComputer.compute()).andReturn(4);
    expect(mockComputer2.compute()).andReturn(4);

    replay(mockComputer, mockComputer2);

    toothPickExtension.inject(this);

    //WHEN
    int result = simpleEntryPointUnderTest.multiply();

    //THEN
    assertThat(result, is(48));
    verify(mockComputer);
  }
}
