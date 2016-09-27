package toothpick.data;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton public class FooProviderAnnotatedSingletonImpl implements Provider<IFoo> {
  public @Inject Bar bar;

  @Inject public FooProviderAnnotatedSingletonImpl() {
  }

  @Override public IFoo get() {
    Foo foo = new Foo();
    foo.bar = bar;
    return foo;
  }
}
