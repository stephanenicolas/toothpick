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

import static java.lang.String.format;

/**
 * A non thread safe internal provider. It should never be exposed outside of Toothpick.
 *
 * @param <T> the class of the instances provided by this provider.
 */
public class ScopedProviderImpl<T> extends InternalProviderImpl<T> {
  protected Scope scope;

  public ScopedProviderImpl(Scope scope, Factory<?> factory) {
    super(factory);
    this.scope = scope;
  }

  public ScopedProviderImpl(
      Scope scope,
      Class<?> factoryKeyClass,
      boolean isProviderFactoryClass,
      boolean isCreatingSingletonInScope,
      boolean isCreatingReleasableInScope,
      boolean isProvidingSingletonInScope,
      boolean isProvidingReleasable) {
    super(
        factoryKeyClass,
        isProviderFactoryClass,
        isCreatingSingletonInScope,
        isCreatingReleasableInScope,
        isProvidingSingletonInScope,
        isProvidingReleasable);
    this.scope = scope;
  }

  // we lock on the unscoped provider itself to prevent concurrent usage
  // of the unscoped provider (
  public T get(Scope scope) {
    return super.get(this.scope);
  }

  @Override
  protected void checkFactoryScope(Factory<?> factory) {
    if (factory.getTargetScope(scope) != scope) {
      String factoryClassName = factory.getClass().getName();
      String factoryKeyClass =
          factoryClassName.substring(0, factoryClassName.length() - "__Factory".length());
      String message =
          format(
              "The class %s has a scope annotation that is not supported by the scope named \"%s\". "
                  + "A binding for a scope annotated class or provider class must use an "
                  + "annotation supported by the scope where the binding is installed. "
                  + "This is also true for classes annotated with @Singleton without additional scope annotation: "
                  + "they can only be installed in a root scope.",
              factoryKeyClass, scope.getName());
      throw new RuntimeException(message);
    }
  }
}
