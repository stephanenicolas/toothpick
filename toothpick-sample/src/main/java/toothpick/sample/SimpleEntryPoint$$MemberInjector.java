package toothpick.sample;

import toothpick.Injector;
import toothpick.MemberInjector;

public class SimpleEntryPoint$$MemberInjector implements MemberInjector<SimpleEntryPoint> {
  @Override public void inject(SimpleEntryPoint simpleEntryPoint, Injector injector) {
    simpleEntryPoint.computer = injector.getInstance(Computer.class);
  }
}
