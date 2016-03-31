package toothpick.sample;

import toothpick.MemberInjector;
import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;

public class MemberInjectorRegistry extends AbstractMemberInjectorRegistry {

  @Override public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
    switch (clazz.getName()) {
      case "toothpick.sample.SimpleEntryPoint":
        return (MemberInjector<T>) new SimpleEntryPoint$$MemberInjector();
    }
    return super.getMemberInjector(clazz);
  }
}
