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
  private ThreadLocal<LinkedHashSet<Entry>> cycleDetectionStack =
      new ThreadLocal<LinkedHashSet<Entry>>() {
        @Override
        protected LinkedHashSet<Entry> initialValue() {
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
  public void checkCyclesStart(Scope scope, Class clazz, String name) {
    final Entry entry = new Entry(scope, clazz, name);
    final LinkedHashSet<Entry> linkedHashSet = cycleDetectionStack.get();
    if (linkedHashSet.contains(entry)) {
      throw new CyclicDependencyException(Entry.getClassList(linkedHashSet), clazz);
    }

    linkedHashSet.add(entry);
  }

  @Override
  public void checkCyclesEnd(Scope scope, Class clazz, String name) {
    cycleDetectionStack.get().remove(new Entry(scope, clazz, name));
  }

  private static class Entry {
    public final Scope scope;
    public final Class clazz;
    public final String name;

    Entry(Scope scope, Class clazz, String name) {
      this.scope = scope;
      this.clazz = clazz;
      this.name = name;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      Entry p = (Entry) o;
      return equal(p.scope, scope) && equal(p.clazz, clazz) && equal(p.name, name);
    }

    @Override
    public int hashCode() {
      return (scope == null ? 0 : scope.hashCode()) ^ (clazz == null ? 0 : clazz.hashCode()) ^ (name == null ? 0 : name.hashCode());
    }

    private boolean equal(Object a, Object b) {
      return (a == b) || (a != null && a.equals(b));
    }

    private static List<Class<?>> getClassList(Collection<Entry> entryCollection) {
      List<Class<?>> classList = new ArrayList<>();
      for (Entry entry : entryCollection) {
        classList.add(entry.clazz);
      }
      return classList;
    }
  }
}
