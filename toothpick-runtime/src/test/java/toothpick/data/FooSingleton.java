package toothpick.data;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton //annotation is not needed, but it's a better example
public class FooSingleton implements IFooSingleton {
  @Inject public Bar bar; //annotation is not needed, but it's a better example

  @Inject public FooSingleton() {
  }
}
