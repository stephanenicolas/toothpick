package toothpick.config;

import java.lang.annotation.Annotation;
import javax.inject.Provider;
import javax.inject.Qualifier;

public class Binding<T> {
  private boolean isScoped;
  private Class<T> key;
  private Mode mode;
  private Class<? extends T> implementationClass;
  private T instance;
  private Provider<? extends T> providerInstance;
  private Class<? extends Provider<? extends T>> providerClass;
  private String name;

  public Binding(Class<T> key) {
    this.key = key;
    mode = Mode.SIMPLE;
  }

  public Binding<T> withName(String name) {
    this.name = name;
    return this;
  }

  public <A extends Annotation> Binding<T> withName(Class<A> annotationClassWithQualifierAnnotation) {
    if (!annotationClassWithQualifierAnnotation.isAnnotationPresent(Qualifier.class)) {
      throw new IllegalArgumentException(
          String.format("Only qualifier annotation annotations can be used to define a binding name. Add @Qualifier to %s",
              annotationClassWithQualifierAnnotation));
    }
    this.name = annotationClassWithQualifierAnnotation.getClass().getName();
    return this;
  }

  public void scope() {
    isScoped = true;
  }

  public BoundState to(Class<? extends T> implClass) {
    this.implementationClass = implClass;
    mode = Mode.CLASS;
    return new BoundState();
  }

  public void to(T instance) {
    this.instance = instance;
    mode = Mode.INSTANCE;
  }

  public void toProvider(Provider<? extends T> providerInstance) {
    this.providerInstance = providerInstance;
    mode = Mode.PROVIDER_INSTANCE;
  }

  public BoundState toProvider(Class<? extends Provider<? extends T>> providerClass) {
    this.providerClass = providerClass;
    mode = Mode.PROVIDER_CLASS;
    return new BoundState();
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

  public Provider<? extends T> getProviderInstance() {
    return providerInstance;
  }

  public Class<? extends Provider<? extends T>> getProviderClass() {
    return providerClass;
  }

  public String getName() {
    return name;
  }

  public boolean isScoped() {
    return isScoped;
  }

  public enum Mode {
    SIMPLE,
    CLASS,
    INSTANCE,
    PROVIDER_INSTANCE,
    PROVIDER_CLASS
  }

  public class BoundState {
    public void scope() {
      isScoped = true;
    }
  }
}
