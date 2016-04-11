package toothpick.inject.provider;

import javax.inject.Provider;
import org.junit.Test;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.ToothPickBaseTest;
import toothpick.data.Bar;
import toothpick.data.FooSingleton;
import toothpick.data.FooWithProvider;
import toothpick.data.FooWithProviderOfSingleton;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Test injection of {@code Provider}s.
 */
public class InjectionOfProviderTest extends ToothPickBaseTest {

  @Test
  public void testSimpleInjection_shouldReturnAProviderOfInstances_whenInjectedClassIsNotAnnotatedWithSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl("");
    FooWithProvider fooWithProvider = new FooWithProvider();

    //WHEN
    injector.inject(fooWithProvider);

    //THEN
    assertThat(fooWithProvider.bar, notNullValue());
    assertThat(fooWithProvider.bar, isA(Provider.class));
    Bar bar1 = fooWithProvider.bar.get();
    assertThat(bar1, isA(Bar.class));
    Bar bar2 = fooWithProvider.bar.get();
    assertThat(bar2, isA(Bar.class));
    assertThat(bar1, not(sameInstance(bar2)));
  }

  @Test
  public void testSimpleInjection_shouldReturnAProviderOfSingleton_whenInjectedClassIsAnnotatedWithSingleton() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl("");
    FooWithProviderOfSingleton fooWithProviderOfSingleton = new FooWithProviderOfSingleton();

    //WHEN
    injector.inject(fooWithProviderOfSingleton);

    //THEN
    assertThat(fooWithProviderOfSingleton.fooSingletonProvider, notNullValue());
    assertThat(fooWithProviderOfSingleton.fooSingletonProvider, isA(Provider.class));
    FooSingleton fooSingleton1 = fooWithProviderOfSingleton.fooSingletonProvider.get();
    assertThat(fooSingleton1, isA(FooSingleton.class));
    FooSingleton fooSingleton2 = fooWithProviderOfSingleton.fooSingletonProvider.get();
    assertThat(fooSingleton2, isA(FooSingleton.class));
    assertThat(fooSingleton2, sameInstance(fooSingleton1));
  }
}
