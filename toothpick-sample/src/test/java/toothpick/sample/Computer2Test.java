package toothpick.sample;

import org.easymock.TestSubject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Computer2Test {

  @TestSubject private Computer2 computer2UnderTest = new Computer2();

  @Test
  public void testMultiply() throws Exception {
    //GIVEN

    //WHEN
    int result = computer2UnderTest.compute();

    //THEN
    assertThat(result, is(2));
  }
}