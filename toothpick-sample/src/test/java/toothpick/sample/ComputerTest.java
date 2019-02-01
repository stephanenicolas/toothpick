package toothpick.sample;

import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import toothpick.testing.ToothPickExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ComputerTest {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "MyScope");

  @Inject Computer computerUnderTest;

  @Test
  void testMultiply() {
    //GIVEN
    toothPickExtension.inject(this);

    //WHEN
    int result = computerUnderTest.compute();

    //THEN
    assertThat(result, is(2));
  }
}
