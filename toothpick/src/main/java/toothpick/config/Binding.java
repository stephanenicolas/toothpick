/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.config;

import androidx.annotation.NonNull;
import java.lang.annotation.Annotation;
import javax.inject.Provider;
import javax.inject.Qualifier;

public class Binding<T> {
  private Mode mode;
  private Class<T> key;
  private String name;
  private Class<? extends T> implementationClass;
  private T instance;
  private Provider<? extends T> providerInstance;
  private Class<? extends Provider<? extends T>> providerClass;
  private boolean isCreatingSingleton;
  private boolean isCreatingReleasable;
  private boolean isProvidingSingleton;
  private boolean isProvidingReleasable;

  public Binding(@NonNull Class<T> key) {
    this.key = key;
    mode = Mode.SIMPLE;
  }

  @NonNull
  public CanBeReleasable singleton() {
    isCreatingSingleton = true;
    return new CanBeReleasable();
  }

  @NonNull
  public Mode getMode() {
    return mode;
  }

  @NonNull
  public Class<T> getKey() {
    return key;
  }

  @NonNull
  public Class<? extends T> getImplementationClass() {
    return implementationClass;
  }

  @NonNull
  public T getInstance() {
    return instance;
  }

  @NonNull
  public Provider<? extends T> getProviderInstance() {
    return providerInstance;
  }

  @NonNull
  public Class<? extends Provider<? extends T>> getProviderClass() {
    return providerClass;
  }

  @NonNull
  public String getName() {
    return name;
  }

  public boolean isCreatingSingleton() {
    return isCreatingSingleton;
  }

  public boolean isProvidingSingleton() {
    return isProvidingSingleton;
  }

  public boolean isCreatingReleasable() {
    return isCreatingReleasable;
  }

  public boolean isProvidingReleasable() {
    return isProvidingReleasable;
  }

  public enum Mode {
    SIMPLE,
    CLASS,
    INSTANCE,
    PROVIDER_INSTANCE,
    PROVIDER_CLASS
  }

  // *************************
  // ***** DSL STATE MACHINE *
  // *************************

  public class CanBeNamed extends CanBeBound {
    @NonNull
    public CanBeBound withName(@NonNull String name) {
      Binding.this.name = name;
      return new CanBeBound();
    }

    @NonNull
    public <A extends Annotation> CanBeBound withName(
        @NonNull Class<A> annotationClassWithQualifierAnnotation) {
      if (!annotationClassWithQualifierAnnotation.isAnnotationPresent(Qualifier.class)) {
        throw new IllegalArgumentException(
            String.format(
                "Only qualifier annotation annotations can be used to define a binding name. Add @Qualifier to %s",
                annotationClassWithQualifierAnnotation));
      }
      Binding.this.name = annotationClassWithQualifierAnnotation.getCanonicalName();
      return new CanBeBound();
    }
  }

  public class CanBeBound {
    public CanBeReleasable singleton() {
      isCreatingSingleton = true;
      return new CanBeReleasable();
    }

    public void toInstance(@NonNull T instance) {
      Binding.this.instance = instance;
      mode = Mode.INSTANCE;
    }

    @NonNull
    public CanBeSingleton to(@NonNull Class<? extends T> implClass) {
      Binding.this.implementationClass = implClass;
      mode = Mode.CLASS;
      return new CanBeSingleton();
    }

    @NonNull
    public CanProvideSingletonOrSingleton toProvider(
        Class<? extends Provider<? extends T>> providerClass) {
      Binding.this.providerClass = providerClass;
      mode = Mode.PROVIDER_CLASS;
      return new CanProvideSingletonOrSingleton();
    }

    @NonNull
    public CanProvideSingleton toProviderInstance(@NonNull Provider<? extends T> providerInstance) {
      Binding.this.providerInstance = providerInstance;
      mode = Mode.PROVIDER_INSTANCE;
      return new CanProvideSingleton();
    }
  }

  public class CanBeSingleton {
    /** to provide a singleton using the binding's scope and reuse it inside the binding's scope */
    @NonNull
    public CanBeReleasable singleton() {
      Binding.this.isCreatingSingleton = true;
      return new CanBeReleasable();
    }
  }

  public class CanBeReleasable {
    /** to make the singleton releasable */
    public void releasable() {
      Binding.this.isCreatingReleasable = true;
    }
  }

  public class CanProvideSingleton {
    @NonNull
    public CanProvideReleasable providesSingleton() {
      isProvidingSingleton = true;
      return new CanProvideReleasable();
    }
  }

  public class CanProvideReleasable {
    public void providesReleasable() {
      Binding.this.isProvidingReleasable = true;
    }
  }

  public class CanProvideSingletonOrSingleton extends CanBeSingleton {
    @NonNull
    public CanProvideReleasableAndThenOnlySingleton providesSingleton() {
      isProvidingSingleton = true;
      return new CanProvideReleasableAndThenOnlySingleton();
    }
  }

  public class CanProvideReleasableAndThenOnlySingleton {
    @NonNull
    public CanBeOnlySingleton providesReleasable() {
      Binding.this.isProvidingReleasable = true;
      return new CanBeOnlySingleton();
    }
  }

  public class CanBeOnlySingleton {
    public void singleton() {
      Binding.this.isCreatingSingleton = true;
    }
  }
}
