package toothpick.data;

import javax.inject.Inject;
import javax.inject.Provider;

public class FooProvider implements Provider<Foo> {
  @Inject Bar bar; //annotation is not needed, but it's a better example

  @Inject
  public FooProvider() {
  }

  @Override
  public Foo get() {
    return new Foo();
  }
}