package toothpick.integration.inject.provider;

import javax.inject.Provider;
import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.config.Binding;
import toothpick.integration.data.Bar;
import toothpick.integration.data.Foo;
import toothpick.integration.data.FooWithProvider;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Test injection of {@code Provider}s.
 */
public class InjectionOfProviderTest {

  @Test public void testSimpleInjection() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl();
    FooWithProvider fooWithProvider = new FooWithProvider();

    //WHEN
    injector.inject(fooWithProvider);

    //THEN
    assertThat(fooWithProvider.bar, notNullValue());
    assertThat(fooWithProvider.bar, isA(Provider.class));
    assertThat(fooWithProvider.bar.get(), isA(Bar.class));
  }
}
