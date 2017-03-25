package toothpick.getInstance;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.Toothpick;
import toothpick.ToothpickBaseTest;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.configuration.CyclicDependencyException;
import toothpick.data.CyclicFoo;
import toothpick.data.CyclicNamedFoo;
import toothpick.data.IFoo;
import toothpick.registries.NoFactoryFoundException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class CycleCheckTest extends ToothpickBaseTest {

  @BeforeClass
  public static void setUp() throws Exception {
    ToothpickBaseTest.setUp();
    Toothpick.setConfiguration(Configuration.forDevelopment().disableReflection());
  }

  @AfterClass
  public static void staticTearDown() throws Exception {
    Toothpick.setConfiguration(Configuration.forProduction().disableReflection());
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

  @Test(expected = NoFactoryFoundException.class)
  public void testCycleDetection_whenGetInstanceFails_shouldCloseCycle() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");

    //WHEN
    try {
      scope.getInstance(IFoo.class);
    } catch (NoFactoryFoundException nfe) {
      nfe.printStackTrace();
    }

    scope.getInstance(IFoo.class);

    //THEN
    fail("Should throw NoFactoryFoundException as IFoo does not have any implementation bound."
        + "But It should not throw CyclicDependencyException as it was removed from the stack.");
  }
}
