package toothpick.sample;

import org.easymock.EasyMockRule;
import org.easymock.TestSubject;
import org.junit.Rule;
import org.junit.Test;
import toothpick.ToothPick;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ComputerTest {

  @Rule public EasyMockRule mocks = new EasyMockRule(this);
  @TestSubject private Computer computerUnderTest = ToothPick.getOrCreateInjector(null, "Computer").getInstance(Computer.class);

  @Test public void testMultiply() throws Exception {
    //GIVEN

    //WHEN
    int result = computerUnderTest.compute();

    //THEN
    assertThat(result, is(2));
  }
}