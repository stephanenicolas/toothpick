package toothpick.configuration;

import toothpick.Factory;
import toothpick.MemberInjector;

interface ReflectionConfiguration {
  <T> Factory<T> getFactory(Class<T> clazz);
  <T> MemberInjector<T> getMemberInjector(Class<T> clazz);
}
