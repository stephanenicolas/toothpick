package toothpick.testing;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import toothpick.Toothpick;

public class SimpleTest {
  public static boolean wasRun;
  @Rule public ToothPickRule toothPickRule = new ToothPickRule(this) {
    @Override
    public Statement apply(Statement base, Description description) {
      wasRun = true;
      return super.apply(base, description);
    }
  };

  @After
  public void tearDown() throws Exception {
    //needs to be performed after test execution
    //not before as rule are initialized before @Before
    Toothpick.reset();
  }

  @Test
  public void test() throws Exception {
  }
}
