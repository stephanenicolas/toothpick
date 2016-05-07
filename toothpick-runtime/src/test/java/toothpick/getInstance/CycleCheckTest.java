package toothpick.getInstance;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import toothpick.CyclicDependencyException;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPick;
import toothpick.ToothPickBaseTest;
import toothpick.data.CyclicFoo;

import static org.junit.Assert.fail;
import static toothpick.Configuration.development;
import static toothpick.Configuration.production;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class CycleCheckTest extends ToothPickBaseTest {

  @BeforeClass
  public static void setUp() throws Exception {
    ToothPick.setConfiguration(development());
    ToothPickBaseTest.setUp();
  }

  @AfterClass
  public static void staticTearDown() throws Exception {
    ToothPick.setConfiguration(production());
  }

  @Test(expected = CyclicDependencyException.class)
  public void testSimpleCycleDetection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");

    //WHEN
    scope.getInstance(CyclicFoo.class);

    //THEN
    fail("Should throw an exception as a cycle is detected");
  }
}
