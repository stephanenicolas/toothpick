package toothpick.sample;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class Computer2Test {

  private Computer2 computer2UnderTest = new Computer2();

  @Test
  void testMultiply() {
    //GIVEN

    //WHEN
    int result = computer2UnderTest.compute();

    //THEN
    assertThat(result, is(2));
  }
}