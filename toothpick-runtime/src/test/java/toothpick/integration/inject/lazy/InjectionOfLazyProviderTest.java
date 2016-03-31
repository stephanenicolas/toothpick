package toothpick.integration.inject.lazy;

import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.Lazy;
import toothpick.integration.ToothPickIntegrationTest;
import toothpick.integration.data.Bar;
import toothpick.integration.data.FooWithLazy;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Test injection of {@code Lazy}s.
 */
public class InjectionOfLazyProviderTest extends ToothPickIntegrationTest {

  @Test public void testSimpleInjection() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl();
    FooWithLazy fooWithLazy = new FooWithLazy();

    //WHEN
    injector.inject(fooWithLazy);

    //THEN
    assertThat(fooWithLazy.bar, notNullValue());
    assertThat(fooWithLazy.bar, isA(Lazy.class));
    Bar bar1 = fooWithLazy.bar.get();
    assertThat(bar1, isA(Bar.class));
    Bar bar2 = fooWithLazy.bar.get();
    assertThat(bar2, isA(Bar.class));
    assertThat(bar2, sameInstance(bar1));
  }
}
