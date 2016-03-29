package toothpick;

import org.junit.Test;

import static org.junit.Assert.fail;

//TODO more unit tests
public class InjectorTest {

  @Test(expected = IllegalStateException.class) public void testToProvider_shoudThrowException_whenBindingIsNull() throws Exception {
    //GIVEN
    InjectorImpl injector = new InjectorImpl();

    //WHEN
    injector.toProvider(null);

    //THEN
    fail("Should not allow null bindings");
  }
}
