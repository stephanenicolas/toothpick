package toothpick;

import java.lang.annotation.Annotation;
import javax.inject.Provider;
import javax.inject.Singleton;
import toothpick.config.Module;

/**
 * <p>
 * A scope is one of the most important concept in ToothPick.
 * It is actually important in Dependency Injection at large and Toothpick
 * exposes it to developers.
 * </p>
 *
 * <p>
 * Conceptually a scope contains {@link toothpick.config.Binding}s & scoped instances :
 * <dl>
 * <dt>binding</dt>
 * <dd>is way to express that a class {@code Foo} is bound to an implementation {@code Bar}.
 * It means that writing {@code @Inject Foo a;} will return a {@code Bar}. Bindings are valid for the scope where there
 * are defined, and inherited by children scopes. Children scopes can override any binding inherited from of a parent.
 * {@link toothpick.config.Module}s allow to define {@link toothpick.config.Binding}s and are installed in scopes.
 * </dd>
 * <dt>scoped instance</dt>
 * <dd>a scoped instance is an instance that is reused for all injection of a given class.
 * Not all bindings create scoped instances. Bindings {@code Foo} to an instance or to a {@link Provider} instance
 * means that those instances will be recycled every time we inject {@code Foo} from this scope. Scoped instances
 * are all lazily initialized on first injection request.
 * </dd>
 * </dl>
 * </p>
 *
 * <p>
 * In toothpick, Scopes create a tree (actually a disjoint forest). Each scope can have children scopes.
 * Operations on the scope tree (adding / removing children, etc.) are non thread safe.
 * The implementation of ToothPick provides a {@code Toothpick} class that wraps these operations in a thread
 * safe way.
 * </p>
 *
 * <p>
 * Scopes can be associated or bound (not related to binding {@code Foo} to {@code Bar}) to an annotation
 * class that is qualified by the {@link javax.inject.Scope} annotation. All classes annotated by this annotation
 * will automatically be scoped by Toothpick in the scope that is associated to them. Their instances will
 * be recycled in this case.
 * </p>
 *
 * <p>
 * Classes that are not annotated with a {@link javax.inject.Scope} annotation, also called un-scoped classes,
 * are not associated to a particular scope and can be used in all scopes. Their instances are not recycled,
 * every injection provides a different instance. Scoping a class by annotation is conceptually
 * exactly the same as binding it to itself in a scope.
 * </p>
 *
 * <p>
 * Scope resolution :
 * when a class is scoped, either by binding it in a module and then installing this module in a scope, or
 * by adding a {@link javax.inject.Scope} annotation, it means that all its dependencies must be found in
 * the scope itself or a parent scope. Otherwise, Toothpick will crash at runtime when first instantiating this
 * class. The only other allowed alternative is to have an un-scoped dependency.
 * </p>
 */
public interface Scope {
  /**
   * @return the name of the scope. It is only used
   * to access a node via the ToothPick class. The name can't be null.
   */
  Object getName();

  /**
   * @return the parentScope of this scope. Can be null for a root scope.
   */
  Scope getParentScope();

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
  Scope getParentScope(Class scopeAnnotationClass);

  /**
   * @return the root scope of this scope.
   * The root scope is the scope itself if the scope has no parent.
   * Otherwise, if it has parents, it is the highest parent in the hierarchy of parents.
   */
  Scope getRootScope();

  /**
   * Binds a {@code scopeAnnotationClass}, to the current scope. The current scope will accept all classes
   * that are scoped using this {@code scopeAnnotationClass}.
   *
   * @param scopeAnnotationClass an annotation that should be qualified by {@link javax.inject.Scope}. If not,
   * an exception is thrown.
   * Note that the {@link Singleton} scope annotation class doesn't need to be bound, it's built-in.
   * @see #getParentScope(Class)
   */
  void bindScopeAnnotation(Class<? extends Annotation> scopeAnnotationClass);

  boolean isBoundToScopeAnnotation(Class<? extends Annotation> scopeAnnotationClass);

  /**
   * Requests an instance via an unnamed binding.
   *
   * @see #getInstance(Class, String)
   * @see toothpick.config.Module
   */
  <T> T getInstance(Class<T> clazz);

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
  <T> T getInstance(Class<T> clazz, String name);

  /**
   * Requests a provider via an unnamed binding.
   *
   * @see #getProvider(Class, String)
   * @see toothpick.config.Module
   */
  <T> Provider<T> getProvider(Class<T> clazz);

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
  <T> Provider<T> getProvider(Class<T> clazz, String name);

  /**
   * Requests a Lazy via an unnamed binding.
   *
   * @see #getLazy(Class, String)
   * @see toothpick.config.Module
   */
  <T> Lazy<T> getLazy(Class<T> clazz);

  /**
   * Returns a {@code Lazy} named {@code name} of {@code clazz} if one provider is scoped in the current
   * scope, or its ancestors. If there is no such provider, the factory associated
   * to the clazz will be used to create one.
   * All {@link javax.inject.Inject} annotated fields of the instance are injected after creation.
   * If the {@code clazz} is annotated with {@link javax.inject.Singleton} then the created provider
   * will be scoped in the root scope of the current scope.
   *
   * @param clazz the class for which to obtain a lazy in the scope of this scope.
   * @param name the name of this instance, if it's null then a unnamed binding is used, otherwise the associated named binding is used.
   * @param <T> the type of {@code clazz}.
   * @return a scoped lazy or a new one using the factory associated to {@code clazz}.
   * Returned lazies are thread safe.
   * @see toothpick.config.Module
   */
  <T> Lazy<T> getLazy(Class<T> clazz, String name);

  /**
   * Allows to install modules.
   *
   * @param modules an array of modules that define bindings.
   * @see #installTestModules
   */
  void installModules(Module... modules);

  /**
   * <em>DO NOT USE IT IN PRODUCTION.</em><br/>
   * Allows to define test modules. These method should only be used for testing.
   * Test modules have precedence over other normal modules, allowing to define stubs/fake/mocks.
   * All bindings defined in a test module cannot be overridden by a future call to {@link #installModules(Module...)}.
   * But they can still be overridden by a future call to  {@link #installTestModules(Module...)}.
   * This method can only be called once in a scope.
   *
   * @param modules an array of modules that define test bindings.
   */
  void installTestModules(Module... modules);
}
