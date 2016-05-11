package toothpick.testing;

import java.lang.reflect.Field;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FieldValueProviderTest {

  @Test(expected = RuntimeException.class)
  public void testGet_shouldFail_whenFieldIsNotAccessible() throws NoSuchFieldException {
    //GIVEN
    Foo foo = new Foo();
    Field fieldS = Foo.class.getDeclaredField("s");
    FieldValueProvider fieldValueProviderUnderTest = new FieldValueProvider(fieldS, foo);
    fieldS.setAccessible(false);

    //WHEN
    fieldValueProviderUnderTest.get();

    //THEN
    fail("Should thrown an exception");
  }

  @Test
  public void testGet_shouldReturnFieldValue() throws NoSuchFieldException {
    //GIVEN
    Foo foo = new Foo();
    Field fieldS = Foo.class.getDeclaredField("s");
    FieldValueProvider fieldValueProviderUnderTest = new FieldValueProvider(fieldS, foo);

    //WHEN
    String s = (String) fieldValueProviderUnderTest.get();

    //THEN
    assertThat(s, is(foo.s));
  }

  public static class Foo {
    private String s = "foo";
  }
}