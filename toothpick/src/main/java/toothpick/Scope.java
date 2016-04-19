package toothpick;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Provider;
import toothpick.config.Module;

import static java.lang.String.format;

/**
 * Allows to create instances of a given class.
 * A note on concurrency :
 * <ul>
 * <li> all scope operations related to the scope tree should be synchronized on the scope itself (TODO)
 * <li> all operations related to the {@code mapClassesToAllProviders} are synchronized on the class of the provider.
 * <li> all providers provided by the public API (including Lazy) should return a thread safe provider (done)
 * but internally, we can live with a non synchronized provider (TODO).
 * </ul>
 */
public abstract class Scope {
  protected Scope parentScope;
  protected Collection<Scope> childrenScopes = new ArrayList<>();
  protected List<Scope> parentScopes = new ArrayList<>();
  protected IdentityHashMap<Class, AllProviders> mapClassesToAllProviders = new IdentityHashMap<>();
  protected Object name;

  public Scope(Object name) {
    this.name = name;
  }

  /**
   * @return the parentScope of this scope. Can be null for a root scope.
   */
  public Scope getParentScope() {
    return parentScope;
  }

  public Collection<Scope> getChildren() {
    return childrenScopes;
  }

  public Object getName() {
    return name;
  }

  public void addChild(Scope child) {
    if (child == null) {
      throw new IllegalArgumentException("Child must be non null.");
    }

    if (child.parentScope == this) {
      return;
    }

    if (child.parentScope != null) {
      throw new IllegalStateException(format("Injector %s already has a parent: %s", child, child.parentScope));
    }

    childrenScopes.add(child);
    child.parentScope = this;
    child.parentScopes = new ArrayList<>();
    child.parentScopes.add(this);
    child.parentScopes.addAll(parentScopes);
  }

  public void removeChild(Scope child) {
    if (child == null) {
      throw new IllegalArgumentException("Child must be non null.");
    }

    if (child.parentScope != this) {
      throw new IllegalStateException(format("Injector %s has a different parent: %s", child, child.parentScope));
    }

    childrenScopes.remove(child);
    //make the ex-child a new root.
    child.parentScopes = new ArrayList<>();
  }

  /**
   * @return the root scope of this scope.
   * The root scope is the scope itself if the scope has no parent.
   * Otherwise, if it has parents, it is the highest parent in the hierarchy of parents.
   */
  protected Scope getRootScope() {
    if (parentScopes.isEmpty()) {
      return this;
    }
    return parentScopes.get(parentScopes.size() - 1);
  }

  /**
   * Obtains the provider of the class {@code clazz} and name {@code bindingName}
   * that is scoped in the current scope, if any.
   * Ancestors are not taken into account.
   *
   * @param clazz the class for which to obtain the scoped provider of this scope, if one is scoped.
   * @param bindingName the name, possibly {@code null}, for which to obtain the scoped provider of this scope, if one is scoped.
   * @param <T> the type of {@code clazz}.
   * @return the scoped provider of this scope for class {@code clazz} and {@code bindingName},
   * if one is scoped, {@code null} otherwise.
   */
  protected <T> Provider<T> getScopedProvider(Class<T> clazz, Object bindingName) {
    synchronized (clazz) {
      AllProviders allProviders = mapClassesToAllProviders.get(clazz);
      if (allProviders == null) {
        return null;
      }
      if (bindingName == null) {
        return allProviders.unNamedProvider;
      }

      Map<String, Provider> mapNameToProvider = (Map<String, Provider>) allProviders.getMapNameToProvider();
      if (mapNameToProvider == null) {
        return null;
      }
      Provider<T> provider = (Provider<T>) mapNameToProvider.get(bindingName);
      return provider;
    }
  }

  /**
   * Install the provider of the class {@code clazz} and name {@code bindingName}
   * in the current scope.
   *
   * @param clazz the class for which to install the scoped provider of this scope.
   * @param bindingName the name, possibly {@code null}, for which to install the scoped provider.
   * @param <T> the type of {@code clazz}.
   */
  protected <T, U extends T> void installProvider(Class<T> clazz, String bindingName, Provider<U> provider) {
    synchronized (clazz) {
      AllProviders allProviders = mapClassesToAllProviders.get(clazz);
      if (allProviders == null) {
        allProviders = new AllProviders();
        mapClassesToAllProviders.put(clazz, allProviders);
      }
      if (bindingName == null) {
        allProviders.setUnNamedProvider(provider);
      } else {
        Map<String, Provider> mapNameToProvider = allProviders.getMapNameToProvider();
        if (mapNameToProvider == null) {
          mapNameToProvider = new HashMap<>();
          allProviders.setMapNameToProvider(mapNameToProvider);
        }
        mapNameToProvider.put(bindingName, provider);
      }
    }
  }

  /**
   * Returns the instance of {@code clazz} if one is scoped in the current
   * scope, or its ancestors. If there is no such instance, the factory associated
   * to the clazz will be used.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created instance
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain an instance in the scope of this scope.
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
   * @param clazz the class for which to obtain a provider in the scope of this scope.
   * @param <T> the type of {@code clazz}.
   * @return a scoped provider or a new one using the factory associated to {@code clazz}.
   */
  public abstract <T> Provider<T> getProvider(Class<T> clazz);

  /**
   * Returns the instance of {@code clazz} named {@code name} if one is scoped in the current
   * scope, or its ancestors. If there is no such instance, the factory associated
   * to the clazz will be used.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created instance
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain an instance in the scope of this scope.
   * @param name the name of this instance.
   * @param <T> the type of {@code clazz}.
   * @return a scoped instance or a new one produced by the factory associated to {@code clazz}.
   * @see toothpick.config.Binding
   */
  public abstract <T> T getInstance(Class<T> clazz, String name);

  /**
   * Returns a named {@code Provider} of {@code clazz} if one is scoped in the current
   * scope, or its ancestors. If there is no such provider, the factory associated
   * to the clazz will be used to create one.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created provider
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain a provider in the scope of this scope.
   * @param name the name of this instance.
   * @param <T> the type of {@code clazz}.
   * @return a scoped provider or a new one using the factory associated to {@code clazz}.
   */
  public abstract <T> Provider<T> getProvider(Class<T> clazz, String name);

  /**
   * Returns a {@code Lazy} of {@code clazz} if one provider is scoped in the current
   * scope, or its ancestors. If there is no such provider, the factory associated
   * to the clazz will be used to create one.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created provider
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain a lazy in the scope of this scope.
   * @param <T> the type of {@code clazz}.
   * @return a scoped lazy or a new one using the factory associated to {@code clazz}.
   * @see #getProvider(Class)
   */
  public abstract <T> Lazy<T> getLazy(Class<T> clazz);

  /**
   * Returns a {@code Lazy} of {@code clazz} if one provider is scoped in the current
   * scope, or its ancestors. If there is no such provider, the factory associated
   * to the clazz will be used to create one.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created provider
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain a lazy in the scope of this scope.
   * @param name the name of this instance.
   * @param <T> the type of {@code clazz}.
   * @return a scoped lazy or a new one using the factory associated to {@code clazz}.
   * @see #getProvider(Class)
   */
  public abstract <T> Lazy<T> getLazy(Class<T> clazz, String name);

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
    for (Class aClass : mapClassesToAllProviders.keySet()) {
      builder.append(aClass.getName());
      builder.append(',');
    }
    builder.deleteCharAt(builder.length() - 1);
    builder.append(']');
    builder.append('\n');

    Iterator<Scope> iterator = childrenScopes.iterator();
    while (iterator.hasNext()) {
      Scope scope = iterator.next();
      boolean isLast = !iterator.hasNext();
      builder.append(isLast ? lastNode : node);
      builder.append(branch);
      String childString = scope.toString();
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

  protected static class AllProviders<T> {
    private Provider<? extends T> unNamedProvider;
    private Map<String, Provider<? extends T>> mapNameToProvider;

    public Map<String, Provider<? extends T>> getMapNameToProvider() {
      return mapNameToProvider;
    }

    public void setMapNameToProvider(Map<String, Provider<? extends T>> mapNameToProvider) {
      this.mapNameToProvider = mapNameToProvider;
    }

    public <U extends T> void setUnNamedProvider(Provider<U> unNamedProvider) {
      this.unNamedProvider = unNamedProvider;
    }
  }
}
