package toothpick.sample;

import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import toothpick.testing.ToothPickExtension;

class SimpleEntryPointTest {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "MyScope");
  @RegisterExtension MockitoExtension mockitoExtension = new MockitoExtension(this);

  @Mock private Computer mockComputer;
  @Mock private Computer2 mockComputer2;

  @Inject SimpleEntryPoint simpleEntryPointUnderTest;

  @Test
  public void testMultiply() throws Exception {
    //GIVEN
    when(mockComputer.compute()).thenReturn(4);
    when(mockComputer2.compute()).thenReturn(4);

    toothPickExtension.inject(this);

    //WHEN
    int result = simpleEntryPointUnderTest.multiply();

    //THEN
    assertThat(result, is(48));
    verify(mockComputer);
  }
}
