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

/**
 * Inject member of an instance of a class. All injected members are gonna be obtained in the scope
 * of the current scope. MemberInjector are discovered via the {@code MemberInjectorLocator}.
 * Implementations are generated during annotation processing. As soon as a class as an {@link
 * javax.inject.Inject} annotated field or method, a member scope is created. All classes that need
 * to be injected via toothpick need to be package private, otherwise we will fall back on
 * reflection and emit a warning at runtime.
 */
public interface MemberInjector<T> {
  /**
   * Injects all fields of an object. This object will be the starting point of an injection
   * sub-graph.
   *
   * @param t the object in which to inject all dependencies.
   * @param scope the scope in which all dependencies of {@code t} will be looked for.
   */
  void inject(T t, Scope scope);
}
