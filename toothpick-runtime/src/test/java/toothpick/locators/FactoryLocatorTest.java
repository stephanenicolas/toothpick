package toothpick.locators;

import org.junit.Before;
import org.junit.Test;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.data.Foo;
import toothpick.data.Qurtz;

import static org.junit.Assert.fail;

public class FactoryLocatorTest {

  @Before
  public void setUp() {
    Toothpick.setConfiguration(Configuration.forProduction());
  }

  @Test
  public void testGetFactory_shouldFindTheFactoryForFoo_whenTheFactoryIsGenerated() {
    //GIVEN
    //WHEN
    FactoryLocator.getFactory(Foo.class);

    //THEN
    // Should not crash
  }

  @Test(expected = NoFactoryFoundException.class)
  public void testGetFactory_shouldThrowAnException_whenTheFActoryIsNotGenerated() {
    //GIVEN
    //WHEN
    FactoryLocator.getFactory(Qurtz.class);

    //THEN
    fail("Should throw an exception");
  }
}
