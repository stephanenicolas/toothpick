package toothpick.getInstance;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.Toothpick;
import toothpick.ToothpickBaseTest;
import toothpick.config.Module;
import toothpick.configuration.CyclicDependencyException;
import toothpick.data.CyclicFoo;
import toothpick.data.CyclicNamedFoo;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static toothpick.configuration.Configuration.forDevelopment;
import static toothpick.configuration.Configuration.forProduction;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class CycleCheckTest extends ToothpickBaseTest {

  @BeforeClass
  public static void setUp() throws Exception {
    ToothpickBaseTest.setUp();
    Toothpick.setConfiguration(forDevelopment());
    ToothpickBaseTest.setUp();
  }

  @AfterClass
  public static void staticTearDown() throws Exception {
    Toothpick.setConfiguration(forProduction());
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

  @Test
  public void testCycleDetection_whenSameClass_and_differentName_shouldNotCrash() throws Exception {
    //GIVEN
    final CyclicNamedFoo instance1 = new CyclicNamedFoo();
    Scope scope = new ScopeImpl("");
    scope.installModules(new Module() {
      {
        bind(CyclicNamedFoo.class).withName("foo").toInstance(instance1);
      }
    });

    //WHEN
    CyclicNamedFoo instance2 = scope.getInstance(CyclicNamedFoo.class);

    //THEN
    // Should not crashed
    assertThat(instance2, notNullValue());
    assertThat(instance2.cyclicFoo, sameInstance(instance1));
  }
}
