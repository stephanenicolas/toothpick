package toothpick.configuration;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import toothpick.Scope;
import toothpick.config.Binding;

import static java.lang.String.format;

class RuntimeCheckOnConfiguration implements RuntimeCheckConfiguration {
  // We need a LIFO structure here, but stack is thread safe and we use thread local,
  // so this property is overkill and LinkedHashSet is faster on retrieval.
  private ThreadLocal<LinkedHashSet<Pair>> cycleDetectionStack = new ThreadLocal<LinkedHashSet<Pair>>() {
    @Override
    protected LinkedHashSet<Pair> initialValue() {
      return new LinkedHashSet<>();
    }
  };

  /**
   * check that a binding's target annotation scope, if present, is supported
   * by the scope {@code scope}.
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
        if (!scope.isBoundToScopeAnnotation(annotationType)) {
          throw new IllegalBindingException(format("Class %s cannot be bound."
                  + " It has a scope annotation: %s that is not supported by current scope: %s",
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
      return a == b || (a != null && a.equals(b));
    }

    private static List<Class> getClassList(Collection<Pair> pairCollection) {
      List<Class> classList = new ArrayList<>();
      for (Pair pair : pairCollection) {
        classList.add(pair.clazz);
      }
      return classList;
    }
  }
}
