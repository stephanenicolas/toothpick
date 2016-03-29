package toothpick.integration.data;

import javax.inject.Inject;
import toothpick.Provider;

public class IFooProvider implements Provider<IFoo> {
  @Inject public Bar bar;

  @Inject public IFooProvider() {
  }

  @Override public IFoo get() {
    return new Foo();
  }
}
