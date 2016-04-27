package toothpick;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Provider;
import javax.inject.Singleton;
import toothpick.config.Module;

import static java.lang.String.format;

/**
 *
 */
public abstract class Scope {
  protected Scope parentScope;
  protected Collection<Scope> childrenScopes = new ArrayList<>();
  protected List<Scope> parentScopes = new ArrayList<>();
  protected Object name;
  protected Set<Class<? extends Annotation>> scopeAnnotationClasses;

  public Scope(Object name) {
    this.name = name;
    if (name.getClass() == Class.class //
        && Annotation.class.isAssignableFrom((Class) name) //
        && isScopeAnnotationClass((Class<? extends Annotation>) name)) {
      bindScopeAnnotation((Class<? extends Annotation>) name);
    }
  }

  /**
   * @return the parentScope of this scope. Can be null for a root scope.
   */
  public Scope getParentScope() {
    return parentScope;
  }

  /**
   * @param scopeAnnotationClass an annotation that should be qualified by {@link javax.inject.Scope}. If not,
   * an exception is thrown.
   * @return the parent {@link Scope} of this scope that is bound to {@code scopeAnnotationClass}.
   * The current {@code scope} (this) can be returned if it is bound to {@code scopeAnnotationClass}.
   * If no such parent exists, it throws an exception. This later case means that something scoped
   * is using a lower scoped dependency, which is conceptually flawed and not allowed in Toothpick.
   * Note that is {@code scopeAnnotationClass} is {@link Singleton}, the root scope is always returned.
   * Thus the {@link Singleton} scope annotation class doesn't need to be bound, it's built-in.
   */
  @SuppressWarnings({ "unused", "used by generated code" })
  public Scope getParentScope(Class scopeAnnotationClass) {
    checkIsAnnotationScope(scopeAnnotationClass);

    if (scopeAnnotationClass == Singleton.class) {
      return getRootScope();
    }

    Scope currentScope = this;
    while (currentScope != null) {
      if (currentScope.isBoundToScopeAnnotation(scopeAnnotationClass)) {
        return currentScope;
      }
      currentScope = currentScope.getParentScope();
    }
    throw new IllegalStateException(format("There is no parent scope of %s bound to scope scopeAnnotationClass %s",
        this.name, scopeAnnotationClass.getName()));
  }

  /**
   * Binds a {@code scopeAnnotationClass}, to the current scope. The current scope will accept all classes
   * that are scoped using this {@code scopeAnnotationClass}.
   * @param scopeAnnotationClass an annotation that should be qualified by {@link javax.inject.Scope}. If not,
   * an exception is thrown.
   * Note that the {@link Singleton} scope annotation class doesn't need to be bound, it's built-in.
   * @see #getParentScope(Class)
   */
  public void bindScopeAnnotation(Class<? extends Annotation> scopeAnnotationClass) {
    checkIsAnnotationScope(scopeAnnotationClass);
    if (scopeAnnotationClass == Singleton.class) {
      throw new IllegalArgumentException(
          String.format("The annotation @Singleton is already bound to the root scope of any scope. It can be bound dynamically."));
    }

    if (scopeAnnotationClasses == null) {
      scopeAnnotationClasses = new HashSet<>();
    }
    scopeAnnotationClasses.add(scopeAnnotationClass);
  }

  private void checkIsAnnotationScope(Class<? extends Annotation> scopeAnnotationClass) {
    if (!isScopeAnnotationClass(scopeAnnotationClass)) {
      throw new IllegalArgumentException(
          String.format("The annotation %s is not a scope annotation, it is not qualified by javax.inject.Scope.",
              scopeAnnotationClass.getName()));
    }
  }

  private boolean isScopeAnnotationClass(Class<? extends Annotation> scopeAnnotationClass) {
    return scopeAnnotationClass.getAnnotation(javax.inject.Scope.class) != null;
  }

  public boolean isBoundToScopeAnnotation(Class<? extends Annotation> scopeAnnotationClass) {
    if (scopeAnnotationClasses == null) {
      return false;
    }
    return scopeAnnotationClasses.contains(scopeAnnotationClass);
  }

  @SuppressWarnings({"unused", "For the sake of completeness of the API."})
  public Collection<Scope> getChildrenScopes() {
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
    child.parentScope = null;
    child.parentScopes.clear();
  }

  /**
   * @return the root scope of this scope.
   * The root scope is the scope itself if the scope has no parent.
   * Otherwise, if it has parents, it is the highest parent in the hierarchy of parents.
   */
  @SuppressWarnings({ "unused", "used by generated code" })
  public Scope getRootScope() {
    if (parentScopes.isEmpty()) {
      return this;
    }
    return parentScopes.get(parentScopes.size() - 1);
  }

  /**
   * Requests an instance via an unnamed binding.
   *
   * @see #getInstance(Class, String)
   * @see toothpick.config.Module
   */
  public abstract <T> T getInstance(Class<T> clazz);

  /**
   * Returns the instance of {@code clazz} named {@code name} if one is scoped in the current
   * scope, or its ancestors. If there is no such instance, the factory associated
   * to the clazz will be used to produce the instance.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   *
   * @param clazz the class for which to obtain an instance in the scope of this scope.
   * @param name the name of this instance, if it's null then a unnamed binding is used, otherwise the associated named binding is used.
   * @param <T> the type of {@code clazz}.
   * @return a scoped instance or a new one produced by the factory associated to {@code clazz}.
   * @see toothpick.config.Binding
   */
  public abstract <T> T getInstance(Class<T> clazz, String name);

  /**
   * Requests a provider via an unnamed binding.
   *
   * @see #getProvider(Class, String)
   * @see toothpick.config.Module
   */
  public abstract <T> Provider<T> getProvider(Class<T> clazz);

  /**
   * Returns a named {@code Provider} named {@code name} of {@code clazz} if one is scoped in the current
   * scope, or its ancestors. If there is no such provider, the factory associated
   * to the clazz will be used to create one.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   *
   * @param clazz the class for which to obtain a provider in the scope of this scope.
   * @param name the name of this instance, if it's null then a unnamed binding is used, otherwise the associated named binding is used.
   * @param <T> the type of {@code clazz}.
   * @return a scoped provider or a new one using the factory associated to {@code clazz}.
   * Returned providers are thread safe.
   * @see toothpick.config.Module
   */
  public abstract <T> Provider<T> getProvider(Class<T> clazz, String name);

  /**
   * Requests a Lazy via an unnamed binding.
   *
   * @see #getLazy(Class, String)
   * @see toothpick.config.Module
   */
  public abstract <T> Lazy<T> getLazy(Class<T> clazz);

  /**
   * Returns a {@code Lazy} named {@code name} of {@code clazz} if one provider is scoped in the current
   * scope, or its ancestors. If there is no such provider, the factory associated
   * to the clazz will be used to create one.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@param clazz} is annotated with {@link javax.inject.Singleton} then the created provider
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain a lazy in the scope of this scope.
   * @param name the name of this instance, if it's null then a unnamed binding is used, otherwise the associated named binding is used.
   * @param <T> the type of {@code clazz}.
   * @return a scoped lazy or a new one using the factory associated to {@code clazz}.
   * Returned lazies are thread safe.
   * @see toothpick.config.Module
   */
  public abstract <T> Lazy<T> getLazy(Class<T> clazz, String name);

  /**
   * <em>DO NOT USE IT IN PRODUCTION.</em><br/>
   * Allows to define test modules. These method should only be used for testing.
   * Test modules have precedence over other normal modules, allowing to define stubs/fake/mocks.
   * All bindings defined in a test module cannot be overridden by a future call to {@link #installModules(Module...)}.
   * But they can still be overridden by a future call to  {@link #installTestModules(Module...)}.
   *
   * @param modules an array of modules that define test bindings.
   */
  public abstract void installTestModules(Module... modules);

  /**
   * Allows to install modules.
   *
   * @param modules an array of modules that define bindings.
   * @See #installTestModules
   */
  public abstract void installModules(Module... modules);
}
