package toothpick.data;

import javax.inject.Inject;
import javax.inject.Provider;

public class FooProviderReusingInstance implements Provider<Foo> {
  private Foo foo;

  @Inject
  FooProviderReusingInstance() {
  }

  @Override
  public Foo get() {
    if (foo == null) {
      foo = new Foo();
    }
    return foo;
  }
}