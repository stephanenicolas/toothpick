package toothpick;

import javax.inject.Provider;
import org.junit.Test;

import static org.junit.Assert.fail;

public class InternalProviderImplTest {

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenInstanceIsNull() {
    //GIVEN
    //WHEN
    new InternalProviderImpl((String) null);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenProviderInstanceIsNull() {
    //GIVEN
    //WHEN
    new InternalProviderImpl((Provider<String>) null, false);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenFactoryIsNull() {
    //GIVEN
    //WHEN
    new InternalProviderImpl((Factory<String>) null, false);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenProviderFactoryIsNull() {
    //GIVEN
    //WHEN
    new InternalProviderImpl((Factory<Provider<String>>) null, true);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenFactoryClassIsNull() {
    //GIVEN
    //WHEN
    new InternalProviderImpl((Class) null, false, false, false);

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateInternalProviderImpl_shouldFail_whenProviderFactoryClassIsNull() {
    //GIVEN
    //WHEN
    new InternalProviderImpl((Class) null, true, false, false);

    //THEN
    fail("Should throw an exception");
  }

  /* TODO we should have unit tests for this
  @Test
  public void testGet() throws Exception {

  }
  */
}