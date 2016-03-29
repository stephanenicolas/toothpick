package toothpick.integration.data;

import javax.inject.Inject;
import toothpick.Provider;

public class FooProvider implements Provider<Foo> {
  @Inject Bar bar; //annotation is not needed, but it's a better example

  @Override public Foo get() {
    return new Foo();
  }
}