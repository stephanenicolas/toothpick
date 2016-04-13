package toothpick.inject.lazy;

import org.junit.Test;
import toothpick.Lazy;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPick;
import toothpick.ToothPickBaseTest;
import toothpick.data.Bar;
import toothpick.data.FooWithLazy;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Test injection of {@code Lazy}s.
 */
public class InjectionOfLazyProviderTest extends ToothPickBaseTest {

  @Test
  public void testSimpleInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    FooWithLazy fooWithLazy = new FooWithLazy();

    //WHEN
    ToothPick.inject(fooWithLazy, scope);

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
