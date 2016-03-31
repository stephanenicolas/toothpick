package toothpick.integration.data;

import javax.inject.Inject;
import javax.inject.Provider;

public class IFooWithBarProvider implements Provider<IFoo> {
  @Inject public Bar bar;

  @Inject public IFooWithBarProvider() {
  }

  @Override public IFoo get() {
    Foo foo = new Foo();
    foo.bar = bar;
    return foo;
  }
}
