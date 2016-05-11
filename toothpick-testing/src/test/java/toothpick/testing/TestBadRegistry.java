package toothpick.testing;

import org.junit.AfterClass;
import org.junit.Test;
import toothpick.ToothPick;

public class TestBadRegistry {

  @AfterClass
  public static void tearDown() throws Exception {
    //needs to be performed after test execution
    //not before as rule are initialized before @Before
    ToothPick.reset();
  }

  @Test(expected = IllegalArgumentException.class)
  public void test() throws Exception {
    new ToothPickRule(this, "Foo").setRootRegistryPackage("foo");
  }
}
