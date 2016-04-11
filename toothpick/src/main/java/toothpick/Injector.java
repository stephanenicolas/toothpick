package toothpick;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import javax.inject.Provider;
import toothpick.config.Module;

import static java.lang.String.format;

/**
 * Allows to inject members of a given instance,
 * create instances of a given class.
 * An injector has its own scope.
 */
public abstract class Injector {
  protected Injector parentInjector;
  protected Collection<Injector> childrenInjector = new ArrayList<>();
  protected List<Injector> parentInjectors = new ArrayList<>();
  protected IdentityHashMap<Class, Provider> scope = new IdentityHashMap<>();
  protected Object name;

  public Injector(Object name) {
    this.name = name;
  }

  /**
   * @return the parentInjector of this injector. Can be null for a root injector.
   */
  public Injector getParentInjector() {
    return parentInjector;
  }

  public Collection<Injector> getChildrenInjector() {
    return childrenInjector;
  }

  public Object getName() {
    return name;
  }

  public void addChildInjector(Injector child) {
    if (child == null) {
      throw new IllegalArgumentException("Child must be non null.");
    }

    if (child.parentInjector != null) {
      throw new IllegalStateException(format("Injector %s already has a parent: %s" + child, child.parentInjector));
    }

    childrenInjector.add(child);
    child.parentInjector = this;
    child.parentInjectors = new ArrayList<>();
    child.parentInjectors.add(this);
    child.parentInjectors.addAll(parentInjectors);
  }

  public void removeChildInjector(Injector child) {
    if (child == null) {
      throw new IllegalArgumentException("Child must be non null.");
    }

    if (child.parentInjector != this) {
      throw new IllegalStateException(format("Injector %s has a different parent: %s" + child, child.parentInjector));
    }

    childrenInjector.remove(child);
    //make the ex-child a new root.
    child.parentInjectors = new ArrayList<>();
  }

  protected Injector getRootInjector() {
    if (parentInjectors.isEmpty()) {
      return this;
    }
    return parentInjectors.get(parentInjectors.size() - 1);
  }

  /**
   * Obtains the provider of the class {@code clazz} that is scoped in the current scope, if any.
   * Ancestors are not taken into account.
   *
   * @param clazz the class for which to obtain the scoped provider of this injector, if one is scoped.
   * @param <T> the type of {@code clazz}.
   * @return the scoped provider of this injector, if one is scoped, {@code Null} otherwise.
   */
  protected <T> Provider<T> getScopedProvider(Class<T> clazz) {
    return scope.get(clazz);
  }

  /**
   * Injects all fields of an object. This object will be the starting point of an injection sub-graph, i.e.
   * all dependencies of this object will be injected as well when created.
   *
   * @param obj the object of which to all fields will be injected.
   * @param <T> the type of {@code clazz}.
   */
  public abstract <T> void inject(T obj);

  /**
   * Returns the instance of {@code clazz} if one is scoped in the current
   * scope, or its ancestors. If there is no such instance, the factory associated
   * to the clazz will be used.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created instance
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain an instance in the scope of this injector.
   * @param <T> the type of {@code clazz}.
   * @return a scoped instance or a new one produced by the factory associated to {@code clazz}.
   */
  public abstract <T> T getInstance(Class<T> clazz);

  /**
   * Returns a {@code Provider} of {@code clazz} if one is scoped in the current
   * scope, or its ancestors. If there is no such provider, the factory associated
   * to the clazz will be used to create one.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created provider
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain a provider in the scope of this injector.
   * @param <T> the type of {@code clazz}.
   * @return a scoped provider or a new one using the factory associated to {@code clazz}.
   */
  public abstract <T> Provider<T> getProvider(Class<T> clazz);

  /**
   * Returns a {@code Lazy} of {@code clazz} if one provider is scoped in the current
   * scope, or its ancestors. If there is no such provider, the factory associated
   * to the clazz will be used to create one.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created provider
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain a lazy in the scope of this injector.
   * @param <T> the type of {@code clazz}.
   * @return a scoped lazy or a new one using the factory associated to {@code clazz}.
   * @see #getProvider(Class)
   */
  public abstract <T> Lazy<T> getLazy(Class<T> clazz);

  /**
   * Returns a {@code Future} of {@code clazz} if one provider is scoped in the current
   * scope, or its ancestors. If there is no such provider, the factory associated
   * to the clazz will be used to create one.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created provider
   * will be scoped in the root scope of the current scope.
   *
   * All future are executed on a background thread pool. TODO make this configurable.
   *
   * @param clazz the class for which to obtain a future in the scope of this injector.
   * @param <T> the type of {@code clazz}.
   * @return a scoped future or a new one using the factory associated to {@code clazz}.
   * @see #getProvider(Class)
   */
  public abstract <T> Future<T> getFuture(Class<T> clazz);

  /**
   * Allows to define test modules. These method should only be used for testing.
   * DO NOT USE IT IN PRODUCTION.
   * Test modules have precedence over other normal modules, allowing to define stubs/fake/mocks.
   * All bindings defined in a test module cannot be overridden by a future call to {@link #installModules(Module...)}.
   * But they can still be overridden by a future call to  {@link #installTestModules(Module...)}.
   *
   * @param modules an array of modules that define test bindings.
   */
  public abstract void installTestModules(Module... modules);

  /**
   * Allows to define modules.
   *
   * @param modules an array of modules that define bindings.
   * @See #installTestModules
   */
  public abstract void installModules(Module... modules);

  @Override
  public String toString() {
    final String branch = "---";
    final char lastNode = '\\';
    final char node = '+';
    final String indent = "    ";

    StringBuilder builder = new StringBuilder();
    builder.append(name);
    builder.append(':');
    builder.append(System.identityHashCode(this));
    builder.append('\n');

    builder.append('[');
    for (Class aClass : scope.keySet()) {
      builder.append(aClass.getName());
      builder.append(',');
    }
    builder.deleteCharAt(builder.length() - 1);
    builder.append(']');
    builder.append('\n');

    Iterator<Injector> iterator = childrenInjector.iterator();
    while (iterator.hasNext()) {
      Injector injector = iterator.next();
      boolean isLast = !iterator.hasNext();
      builder.append(isLast ? lastNode : node);
      builder.append(branch);
      String childString = injector.toString();
      String[] split = childString.split("\n");
      for (int i = 0; i < split.length; i++) {
        String childLine = split[i];
        if (i != 0) {
          builder.append(indent);
        }
        builder.append(childLine);
        builder.append('\n');
      }
    }

    return builder.toString();
  }
}
