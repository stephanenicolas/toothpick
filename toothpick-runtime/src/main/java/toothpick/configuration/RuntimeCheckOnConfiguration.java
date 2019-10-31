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
package toothpick.configuration;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import toothpick.Scope;
import toothpick.config.Binding;

class RuntimeCheckOnConfiguration implements RuntimeCheckConfiguration {
  // We need a LIFO structure here, but stack is thread safe and we use thread local,
  // so this property is overkill and LinkedHashSet is faster on retrieval.
  private ThreadLocal<LinkedHashSet<Pair>> cycleDetectionStack =
      new ThreadLocal<LinkedHashSet<Pair>>() {
        @Override
        protected LinkedHashSet<Pair> initialValue() {
          return new LinkedHashSet<>();
        }
      };

  /**
   * check that a binding's target annotation scope, if present, is supported by the scope {@code
   * scope}.
   *
   * @param binding the binding being installed.
   * @param scope the scope where the binding is installed.
   */
  @Override
  public void checkIllegalBinding(Binding binding, Scope scope) {
    Class<?> clazz;
    switch (binding.getMode()) {
      case SIMPLE:
        clazz = binding.getKey();
        break;
      case CLASS:
        clazz = binding.getImplementationClass();
        break;
      case PROVIDER_CLASS:
        clazz = binding.getProviderClass();
        break;
      default:
        return;
    }

    for (Annotation annotation : clazz.getAnnotations()) {
      Class<? extends Annotation> annotationType = annotation.annotationType();
      if (annotationType.isAnnotationPresent(javax.inject.Scope.class)) {
        if (!scope.isScopeAnnotationSupported(annotationType)) {
          throw new IllegalBindingException(
              format(
                  "Class %s cannot be scoped."
                      + " It has a scope annotation: %s that is not supported by the current scope: %s",
                  clazz.getName(), annotationType.getName(), scope.getName()));
        }
      }
    }
  }

  @Override
  public void checkCyclesStart(Class clazz, String name) {
    final Pair pair = new Pair(clazz, name);
    final LinkedHashSet<Pair> linkedHashSet = cycleDetectionStack.get();
    if (linkedHashSet.contains(pair)) {
      throw new CyclicDependencyException(Pair.getClassList(linkedHashSet), clazz);
    }

    linkedHashSet.add(pair);
  }

  @Override
  public void checkCyclesEnd(Class clazz, String name) {
    cycleDetectionStack.get().remove(new Pair(clazz, name));
  }

  private static class Pair {
    public final Class clazz;
    public final String name;

    Pair(Class clazz, String name) {
      this.clazz = clazz;
      this.name = name;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Pair)) {
        return false;
      }
      Pair p = (Pair) o;
      return equal(p.clazz, clazz) && equal(p.name, name);
    }

    @Override
    public int hashCode() {
      return (clazz == null ? 0 : clazz.hashCode()) ^ (name == null ? 0 : name.hashCode());
    }

    private boolean equal(Object a, Object b) {
      return (a == b) || (a != null && a.equals(b));
    }

    private static List<Class<?>> getClassList(Collection<Pair> pairCollection) {
      List<Class<?>> classList = new ArrayList<>();
      for (Pair pair : pairCollection) {
        classList.add(pair.clazz);
      }
      return classList;
    }
  }
}
