package toothpick.config;

import java.lang.annotation.Annotation;
import javax.inject.Provider;
import javax.inject.Qualifier;

public class Binding<T> {
  private boolean isCreatingInstancesInScope;
  private boolean isCreatingSingletonInScope;
  private boolean isProvidingSingletonInScope;
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
    this.name = annotationClassWithQualifierAnnotation.getName();
    return this;
  }

  public void instancesInScope() {
    isCreatingInstancesInScope = true;
  }

  public void singletonInScope() {
    isCreatingInstancesInScope = true;
    isCreatingSingletonInScope = true;
  }

  public BoundStateForClassBinding to(Class<? extends T> implClass) {
    this.implementationClass = implClass;
    mode = Mode.CLASS;
    return new BoundStateForClassBinding();
  }

  public void toInstance(T instance) {
    this.instance = instance;
    mode = Mode.INSTANCE;
  }

  public BoundStateForProviderClassBinding toProvider(Class<? extends Provider<? extends T>> providerClass) {
    this.providerClass = providerClass;
    mode = Mode.PROVIDER_CLASS;
    return new BoundStateForProviderClassBinding();
  }

  public BoundStateForProviderInstanceBinding toProviderInstance(Provider<? extends T> providerInstance) {
    this.providerInstance = providerInstance;
    mode = Mode.PROVIDER_INSTANCE;
    return new BoundStateForProviderInstanceBinding();
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

  public boolean isCreatingInstancesInScope() {
    return isCreatingInstancesInScope;
  }

  public boolean isCreatingSingletonInScope() {
    return isCreatingSingletonInScope;
  }

  public boolean isProvidingSingletonInScope() {
    return isProvidingSingletonInScope;
  }

  public enum Mode {
    SIMPLE,
    CLASS,
    INSTANCE,
    PROVIDER_INSTANCE,
    PROVIDER_CLASS
  }

  //*************************
  //***** DSL STATE MACHINE *
  //*************************

  public class BoundStateForClassBinding {
    /**
     * to create instances using the binding's scope
     */
    public void instancesInScope() {
      Binding.this.instancesInScope();
    }

    /**
     * to create a singleton using the binding's scope
     * and reuse it inside the binding's scope
     */
    public void singletonInScope() {
      Binding.this.singletonInScope();
    }
  }

  public class BoundStateForProviderClassBinding extends BoundStateForClassBinding {
    /**
     * to provide a singleton using the binding's scope
     * and reuse it inside the binding's scope
     */
    public void providesSingletonInScope() {
      Binding.this.singletonInScope();
      isProvidingSingletonInScope = true;
    }
  }

  public class BoundStateForProviderInstanceBinding {
    /**
     * to provide a singleton using the binding's scope
     * and reuse it inside the binding's scope
     */
    public void providesSingletonInScope() {
      isProvidingSingletonInScope = true;
    }
  }
}
