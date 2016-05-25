package toothpick.getInstance;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import toothpick.CyclicDependencyException;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.Toothpick;
import toothpick.ToothpickBaseTest;
import toothpick.data.CyclicFoo;

import static org.junit.Assert.fail;
import static toothpick.Configuration.development;
import static toothpick.Configuration.production;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class CycleCheckTest extends ToothpickBaseTest {

  @BeforeClass
  public static void setUp() throws Exception {
    ToothpickBaseTest.setUp();
    Toothpick.setConfiguration(development());
    ToothpickBaseTest.setUp();
  }

  @AfterClass
  public static void staticTearDown() throws Exception {
    Toothpick.setConfiguration(production());
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
