package toothpick.locators;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class NoFactoryFoundExceptionTest {

  @Test
  public void testConstructor_shouldCreateMessage_whenPassedAClass() throws Exception {
    //GIVEN

    //WHEN
    NoFactoryFoundException exception = new NoFactoryFoundException(String.class);

    //THEN
    assertThat(exception.getMessage(), notNullValue());
  }

  @Test
  public void testConstructor_shouldCreateCauseAndMessage_whenPassedAClassAndACause() throws Exception {
    //GIVEN

    //WHEN
    Throwable cause = new Throwable();
    NoFactoryFoundException exception = new NoFactoryFoundException(String.class, cause);

    //THEN
    assertThat(exception.getMessage(), notNullValue());
    assertThat(exception.getCause(), is(cause));
  }
}