package toothpick.config;

import java.lang.annotation.Annotation;
import javax.inject.Provider;

public class Binding<T> {
  private Class<T> key;
  private Mode mode;
  private Class<? extends T> implementationClass;
  private T instance;
  private Provider<T> providerInstance;
  private Class<? extends Provider<T>> providerClass;
  private Object name;

  public Binding(Class<T> key) {
    this.key = key;
    mode = Mode.SIMPLE;
  }

  public Binding<T> withName(String name) {
    this.name = name;
    return this;
  }

  public <A extends Annotation> Binding<T> withName(Class<A> name) {
    this.name = name;
    return this;
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

  public Object getName() {
    return name;
  }

  public enum Mode {
    SIMPLE,
    CLASS,
    INSTANCE,
    PROVIDER_INSTANCE,
    PROVIDER_CLASS;
  }
}
