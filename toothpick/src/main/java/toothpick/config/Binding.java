package toothpick.config;

import toothpick.Provider;

public class Binding<T> {
  private Class<T> key;
  private Mode mode;
  private Class<? extends T> implementationClass;
  private T instance;
  private Provider<T> providerInstance;
  private Class<? extends Provider<T>> providerClass;

  public Binding(Class<T> key) {
    this.key = key;
    mode = Mode.SIMPLE;
  }

  public <IMPL extends T> void to(Class<IMPL> implClass) {
    this.implementationClass = implClass;
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

  public Mode getMode() {
    return mode;
  }

  public Class<T> getKey() {
    return key;
  }

  public Class<? extends T> getImplementationClass() {
    return implementationClass;
  }

  public T getInstance() {
    return instance;
  }

  public Provider<T> getProviderInstance() {
    return providerInstance;
  }

  public Class<? extends Provider<T>> getProviderClass() {
    return providerClass;
  }

  public enum Mode {
    SIMPLE,
    CLASS,
    INSTANCE,
    PROVIDER_INSTANCE,
    PROVIDER_CLASS;
  }
}
