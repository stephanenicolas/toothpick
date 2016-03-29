package toothpick.integration.inject;

import javax.inject.Inject;
import org.junit.Test;
import toothpick.Factory;
import toothpick.Injector;
import toothpick.InjectorImpl;
import toothpick.MemberInjector;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Creates a instance in the simplest possible way
  * without any module.
 */
public class InjectionWithoutModuleTest {

  @Test public void testSimpleInjection() throws Exception {
    //GIVEN
    Injector injector = new InjectorImpl(null);
    Foo foo = new Foo();

    //WHEN
    injector.inject(foo);

    //THEN
    assertThat(foo.bar, notNullValue());
    assertThat(foo.bar, isA(Bar.class));
  }

  public static class Foo {
    @Inject Bar bar; //annotation is not needed, but it's a better example

    public Foo() {
    }
  }

  public static class Bar {
    @Inject public Bar() {
    }
  }

  @SuppressWarnings("unused") public static class Foo$$MemberInjector implements MemberInjector<Foo> {
    @Override public void inject(Foo foo, Injector injector) {
      foo.bar = injector.createInstance(Bar.class);
    }
  }

  @SuppressWarnings("unused") public static class Bar$$Factory implements Factory<Bar> {
    @Override public Bar createInstance(Injector injector) {
      return new Bar();
    }

    @Override public boolean hasSingletonAnnotation() {
      return false;
    }

    @Override public boolean hasProducesSingletonAnnotation() {
      return false;
    }
  }
}
