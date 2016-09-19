package toothpick;

import org.junit.Before;
import toothpick.configuration.Configuration;

/**
 * Test all possible ways to bind stuff in modules with the the forDevelopment configurarion.
 *
 * @see AllBindingsTest
 */
public class AllBindingsTest2 extends AllBindingsTest {
  @Before
  public void setUpForDevelopmentConfiguration() throws Exception {
    Toothpick.setConfiguration(Configuration.forDevelopment());
  }
}
