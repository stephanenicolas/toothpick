/*
 * Copyright 2016 Stephane Nicolas
 * Copyright 2016 Daniel Molinero Reguerra
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
public class InternalProviderImpl<T> {
  /*VisibleForTesting*/ volatile T instance;
  private Factory<T> factory;
  private Class<T> factoryClass;
  /*VisibleForTesting*/ volatile Provider<? extends T> providerInstance;
  private Factory<Provider<T>> providerFactory;
  private Class<Provider<T>> providerFactoryClass;

  private boolean isSingleton;
  private boolean isReleasable;
  protected boolean isProvidingSingleton;
  private boolean isProvidingReleasable;

  public InternalProviderImpl(T instance) {
    // not that an instance cannot be releasable as TP wouldn't know how to recreate a second
    // instance.
    if (instance == null) {
      throw new IllegalArgumentException("The instance can't be null.");
    }

    this.instance = instance;
    this.isSingleton = true;
  }

  public InternalProviderImpl(
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

  public InternalProviderImpl(Factory<?> factory) {
    if (factory == null) {
      throw new IllegalArgumentException("The factory can't be null.");
    }

    this.factory = (Factory<T>) factory;
    this.isSingleton = factory.hasSingletonAnnotation();
    this.isReleasable = this.isSingleton && factory.hasReleasableAnnotation();
    this.isProvidingSingleton = factory.hasProvidesSingletonInScopeAnnotation();
    this.isProvidingReleasable =
        this.isProvidingSingleton && factory.hasProvidesReleasableAnnotation();
  }

  public InternalProviderImpl(
      Class<?> factoryKeyClass,
      boolean isProviderFactoryClass,
      boolean isSingleton,
      boolean isReleasable,
      boolean isProvidingSingleton,
      boolean isProvidingReleasable) {
    if (factoryKeyClass == null) {
      throw new IllegalArgumentException("The factory class can't be null.");
    }

    if (isProviderFactoryClass) {
      this.providerFactoryClass = (Class<Provider<T>>) factoryKeyClass;
      this.isProvidingSingleton = isProvidingSingleton;
      this.isProvidingReleasable = this.isProvidingSingleton && isProvidingReleasable;
    } else {
      this.factoryClass = (Class<T>) factoryKeyClass;
    }
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
      checkFactoryScope(factory);
      this.isSingleton |= factory.hasSingletonAnnotation();
      this.isReleasable |= (this.isSingleton && factory.hasReleasableAnnotation());
      // gc
      factoryClass = null;
    }

    if (factory != null) {
      if (isSingleton) {
        instance = factory.createInstance(scope);
        // gc
        factory = null;
        return instance;
      }

      return factory.createInstance(scope);
    }

    if (providerFactoryClass != null && providerFactory == null) {
      providerFactory = FactoryLocator.getFactory(providerFactoryClass);
      checkFactoryScope(providerFactory);
      this.isSingleton |= providerFactory.hasSingletonAnnotation();
      this.isReleasable |= (this.isSingleton && providerFactory.hasReleasableAnnotation());
      this.isProvidingSingleton |= providerFactory.hasProvidesSingletonInScopeAnnotation();
      this.isProvidingReleasable |=
          (this.isProvidingSingleton && providerFactory.hasProvidesReleasableAnnotation());

      // gc
      providerFactoryClass = null;
    }

    if (providerFactory != null) {
      if (isSingleton) {
        providerInstance = providerFactory.createInstance(scope);
        // gc
        providerFactory = null;
        if (isProvidingSingleton) {
          instance = providerInstance.get();
          return instance;
        }
        return providerInstance.get();
      }

      if (isProvidingSingleton) {
        instance = providerFactory.createInstance(scope).get();
        // gc
        providerFactory = null;
        return instance;
      }

      return providerFactory.createInstance(scope).get();
    }

    throw new IllegalStateException(
        "A provider can only be used with an instance, a provider, a factory or a provider factory. Should not happen.");
  }

  protected void checkFactoryScope(Factory<?> factory) {
    // do nothing, will be overriden.
  }

  public boolean isReleasable() {
    return isReleasable || isProvidingReleasable;
  }

  public void release() {
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
