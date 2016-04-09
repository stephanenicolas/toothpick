package toothpick.data;

import java.util.concurrent.Future;
import javax.inject.Inject;

public class FooWithFuture implements IFoo {
  @Inject public Future<Bar> bar; //annotation is not needed, but it's a better example

  public FooWithFuture() {
  }
}
