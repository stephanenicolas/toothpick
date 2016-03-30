package toothpick.integration.inject.provider;

import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.integration.data.Bar;
import toothpick.integration.data.Foo;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Test injection of providers.
 */
public class InjectionOfProviderTest {

  @Test public void testSimpleInjection() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl();
    Foo foo = new Foo();

    //WHEN
    injector.inject(foo);

    //THEN
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }
}
