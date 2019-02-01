package toothpick.locators;

import org.junit.Before;
import org.junit.Test;
import toothpick.MemberInjector;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.data.Foo;
import toothpick.data.Qurtz;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class MemberInjectorLocatorTest {

  @Before
  public void setUp() {
    Toothpick.setConfiguration(Configuration.forProduction());
  }

  @Test
  public void testGetMemberInjector_shouldFindTheMemberInjectorForFoo_whenTheMemberInjectorIsGenerated() {
    //GIVEN
    //WHEN
    MemberInjector<Foo> memberInjector = MemberInjectorLocator.getMemberInjector(Foo.class);

    //THEN
    assertThat(memberInjector, notNullValue());
  }

  @Test
  public void testGetMemberInjector_shouldNotReturnNull_whenTheMemberInjectorIsNotGenerated() {
    //GIVEN
    //WHEN
    MemberInjector<Qurtz> memberInjector = MemberInjectorLocator.getMemberInjector(Qurtz.class);

    //THEN
    assertThat(memberInjector, nullValue());
  }
}
