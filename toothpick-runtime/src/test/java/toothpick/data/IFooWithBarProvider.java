package toothpick.data;

import javax.inject.Inject;
import javax.inject.Provider;

public class IFooWithBarProvider implements Provider<IFoo> {
  private Bar bar;

  @Inject public IFooWithBarProvider(Bar bar) {
    this.bar = bar;
  }

  @Override public IFoo get() {
    Foo foo = new Foo();
    foo.bar = bar;
    return foo;
  }
}
