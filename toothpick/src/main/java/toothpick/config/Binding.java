package toothpick.config;

import toothpick.Factory;
import toothpick.FactoryRegistry;
import toothpick.Provider;
import toothpick.providers.NonAnnotatedProviderClassPoweredProvider;
import toothpick.providers.NonSingletonAnnotatedClassPoweredProvider;
import toothpick.providers.ProducesSingletonAnnotatedProviderClassPoweredProvider;
import toothpick.providers.SingletonAnnotatedClassPoweredProvider;
import toothpick.providers.SingletonAnnotatedProviderClassPoweredProvider;
import toothpick.providers.SingletonPoweredProvider;

import static java.lang.String.format;

public class Binding<T> {
  private Class<T> key;
  private Mode mode;
  private Class<? extends T> implClass;
  private T instance;
  private Provider<T> providerInstance;
  private Class<? extends Provider<T>> providerClass;

  public Binding(Class<T> key) {
    this.key = key;
    mode = Mode.SIMPLE;
  }

  public <IMPL extends T> void to(Class<IMPL> implClass) {
    this.implClass = implClass;
    mode = Mode.CLASS;
  }

  public void to(T instance) {
    this.instance = instance;
    mode = Mode.INSTANCE;
  }

  public void toProvider(Provider<T> providerInstance) {
    this.providerInstance = providerInstance;
    mode = Mode.PROVIDER_INSTANCE;
  }

  public void toProvider(Class<? extends Provider<T>> providerClass) {
    this.providerClass = providerClass;
    mode = Mode.PROVIDER_CLASS;
  }

  public Provider<T> toProvider() {
    switch (mode) {
      case SIMPLE:
        return new SingletonAnnotatedClassPoweredProvider<>(key, key);
      case CLASS:
        Factory<? extends T> factory = FactoryRegistry.getFactory(implClass);
        if (factory.hasSingletonAnnotation()) {
          return new SingletonAnnotatedClassPoweredProvider(key, implClass);
        } else {
          return new NonSingletonAnnotatedClassPoweredProvider<>(key, implClass);
        }
      case INSTANCE:
        return new SingletonPoweredProvider<>(instance);
      case PROVIDER_INSTANCE:
        return providerInstance;
      case PROVIDER_CLASS:
        Factory<? extends Provider<T>> providerFactory = FactoryRegistry.getFactory(providerClass);
        if (providerFactory.hasSingletonAnnotation()) {
          return new SingletonAnnotatedProviderClassPoweredProvider(key, providerClass);
        } else if (providerFactory.hasProducesSingletonAnnotation()) {
          return new ProducesSingletonAnnotatedProviderClassPoweredProvider<>(key, providerClass);
        } else {
          return new NonAnnotatedProviderClassPoweredProvider<>(key, providerClass);
        }

      default:
        throw new IllegalStateException(
            format("mode is not handled: %s. This should not happen.", mode));
    }
  }

  private enum Mode {
    SIMPLE,
    CLASS,
    INSTANCE,
    PROVIDER_INSTANCE,
    PROVIDER_CLASS;
  }
}
