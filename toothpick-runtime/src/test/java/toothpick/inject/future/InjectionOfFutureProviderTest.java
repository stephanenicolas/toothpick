package toothpick.inject.future;

import java.util.concurrent.Future;
import org.junit.Test;
import toothpick.Scope;
import toothpick.ScopeImpl;
import toothpick.ToothPick;
import toothpick.ToothPickBaseTest;
import toothpick.data.Bar;
import toothpick.data.FooWithFuture;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Test injection of {@code Lazy}s.
 */
public class InjectionOfFutureProviderTest extends ToothPickBaseTest {

  @Test
  public void testSimpleInjection() throws Exception {
    //GIVEN
    Scope scope = new ScopeImpl("");
    FooWithFuture fooWithFuture = new FooWithFuture();

    //WHEN
    ToothPick.inject(fooWithFuture, scope);

    //THEN
    assertThat(fooWithFuture.bar, notNullValue());
    assertThat(fooWithFuture.bar, isA(Future.class));
    Bar bar1 = fooWithFuture.bar.get();
    assertThat(bar1, isA(Bar.class));
    Bar bar2 = fooWithFuture.bar.get();
    assertThat(bar2, isA(Bar.class));
    assertThat(bar2, sameInstance(bar1));
  }
}
