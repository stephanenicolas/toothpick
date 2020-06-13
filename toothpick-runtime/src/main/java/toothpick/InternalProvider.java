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
package toothpick;

import javax.inject.Provider;
import toothpick.locators.FactoryLocator;

/**
 * A non thread safe internal provider. It should never be exposed outside of Toothpick.
 *
 * @param <T> the class of the instances provided by this provider.
 */
public class InternalProvider<T> {
  /*VisibleForTesting*/ volatile T instance;
  private Factory<? extends T> factory;
  private Class<? extends T> factoryClass;
  /*VisibleForTesting*/ volatile Provider<? extends T> providerInstance;
  private Factory<? extends Provider<? extends T>> providerFactory;
  private Class<? extends Provider<? extends T>> providerFactoryClass;

  private boolean isSingleton;
  private boolean isReleasable;
  private boolean isProvidingSingleton;
  private boolean isProvidingReleasable;

  InternalProvider(T instance) {
    // not that an instance cannot be releasable as TP wouldn't know how to recreate a second
    // instance.
    if (instance == null) {
      throw new IllegalArgumentException("The instance can't be null.");
    }

    this.instance = instance;
    this.isSingleton = true;
  }

  InternalProvider(
      Provider<? extends T> providerInstance,
      boolean isProvidingSingleton,
      boolean isProvidingReleasable) {
    // not that an instance of provider cannot be releasable as TP wouldn't know how to recreate a
    // second instance.
    // but it can provide releasable singletons
    if (providerInstance == null) {
      throw new IllegalArgumentException("The provider can't be null.");
    }

    this.providerInstance = providerInstance;
    this.isSingleton = true;
    this.isProvidingSingleton = isProvidingSingleton;
    this.isProvidingReleasable = isProvidingSingleton && isProvidingReleasable;
  }

  InternalProvider(Factory<T> factory) {
    if (factory == null) {
      throw new IllegalArgumentException("The factory can't be null.");
    }

    this.factory = factory;
    this.isSingleton = factory.hasSingletonAnnotation();
    this.isReleasable = this.isSingleton && factory.hasReleasableAnnotation();
    this.isProvidingSingleton = factory.hasProvidesSingletonAnnotation();
    this.isProvidingReleasable =
        this.isProvidingSingleton && factory.hasProvidesReleasableAnnotation();
  }

  InternalProvider(Class<? extends T> factoryKeyClass, boolean isSingleton, boolean isReleasable) {
    if (factoryKeyClass == null) {
      throw new IllegalArgumentException("The factory class can't be null.");
    }

    this.factoryClass = factoryKeyClass;
    this.isSingleton = isSingleton;
    this.isReleasable = this.isSingleton && isReleasable;
  }

  InternalProvider(
      Class<? extends Provider<? extends T>> factoryKeyClass,
      boolean isSingleton,
      boolean isReleasable,
      boolean isProvidingSingleton,
      boolean isProvidingReleasable) {
    if (factoryKeyClass == null) {
      throw new IllegalArgumentException("The factory class can't be null.");
    }

    this.providerFactoryClass = factoryKeyClass;
    this.isProvidingSingleton = isProvidingSingleton;
    this.isProvidingReleasable = this.isProvidingSingleton && isProvidingReleasable;
    this.isSingleton = isSingleton;
    this.isReleasable = this.isSingleton && isReleasable;
  }

  // we lock on the unscoped provider itself to prevent concurrent usage
  // of the unscoped provider (
  public synchronized T get(Scope scope) {
    if (instance != null) {
      return instance;
    }

    if (providerInstance != null) {
      if (isProvidingSingleton) {
        instance = providerInstance.get();
        return instance;
      }

      return providerInstance.get();
    }

    if (factoryClass != null && factory == null) {
      factory = FactoryLocator.getFactory(factoryClass);
      this.isSingleton |= factory.hasSingletonAnnotation();
      this.isReleasable |= (this.isSingleton && factory.hasReleasableAnnotation());
      // gc
      factoryClass = null;
    }

    if (factory != null) {
      if (isSingleton) {
        instance = factory.createInstance(scope);

        if (!isReleasable) {
          // gc
          factory = null;
        }

        return instance;
      }

      return factory.createInstance(scope);
    }

    if (providerFactoryClass != null && providerFactory == null) {
      providerFactory = FactoryLocator.getFactory(providerFactoryClass);
      this.isSingleton |= providerFactory.hasSingletonAnnotation();
      this.isReleasable |= (this.isSingleton && providerFactory.hasReleasableAnnotation());
      this.isProvidingSingleton |= providerFactory.hasProvidesSingletonAnnotation();
      this.isProvidingReleasable |=
          (this.isProvidingSingleton && providerFactory.hasProvidesReleasableAnnotation());

      // gc
      providerFactoryClass = null;
    }

    if (providerFactory != null) {
      if (isSingleton) {
        providerInstance = providerFactory.createInstance(scope);

        if (!isReleasable) {
          // gc
          providerFactory = null;
        }

        if (isProvidingSingleton) {
          instance = providerInstance.get();
          return instance;
        }
        return providerInstance.get();
      }

      if (isProvidingSingleton) {
        instance = providerFactory.createInstance(scope).get();

        if (!isProvidingReleasable) {
          // gc
          providerFactory = null;
        }
        return instance;
      }

      return providerFactory.createInstance(scope).get();
    }

    throw new IllegalStateException(
        "A provider can only be used with an instance, a provider, a factory or a provider factory. Should not happen.");
  }

  boolean isReleasable() {
    return isReleasable || isProvidingReleasable;
  }

  void release() {
    if (isReleasable) {
      if (providerInstance != null) {
        providerInstance = null;
      } else {
        instance = null;
      }
    }
    if (isProvidingReleasable) {
      instance = null;
    }
  }
}
