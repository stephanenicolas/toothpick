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

/** Allows to inject members of a given instance. An injector works with a scope. */
public interface Injector {
  /**
   * Injects all {@link javax.inject.Inject} members of an object. This object will be the starting
   * point of an injection sub-graph, i.e. all dependencies of this object will be injected as well
   * when created.
   *
   * <p>All {@link javax.inject.Inject} annotated fields will be assigned, all {@link
   * javax.inject.Inject} annotated methods will be called. All required dependencies will be
   * created inside {@code scope}.
   *
   * <p>Injection supports any type of data, including those without injection at all. This allows
   * Toothpick to fully support polymorphism (in case a super class defines {@link
   * javax.inject.Inject} annotated members but not a subclass, we still allow to inject the
   * subclass instances. They will get injected as injected of the super class).
   *
   * @param obj the object into which all members will be injected.
   * @param scope the scope in which all dependencies are obtained.
   * @param <T> the type of {@code clazz}.
   */
  <T> void inject(T obj, Scope scope);
}
