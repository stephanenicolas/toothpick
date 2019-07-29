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

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.inject.Provider;
import javax.inject.Singleton;
import toothpick.config.Module;

/**
 * A scope is one of the most important concept in Toothpick. It is actually important in Dependency
 * Injection at large and Toothpick exposes it to developers.
 *
 * <p>Conceptually a scope contains {@link toothpick.config.Binding}s & scoped instances :
 *
 * <dl>
 *   <dt>binding
 *   <dd>is way to express that a class {@code Foo} is bound to an implementation {@code Bar}. It
 *       means that writing {@code @Inject Foo a;} will return a {@code Bar}. Bindings are valid for
 *       the scope where there are defined, and inherited by children scopes. Children scopes can
 *       override any binding inherited from of a parent. {@link Module}s allow to define {@link
 *       toothpick.config.Binding}s and are installed in scopes.
 *   <dt>scoped instance
 *   <dd>a scoped instance is an instance that is reused for all injection of a given class. Not all
 *       bindings create scoped instances. Bindings {@code Foo} to an instance or to a {@link
 *       Provider} instance means that those instances will be recycled every time we inject {@code
 *       Foo} from this scope. Scoped instances are all lazily initialized on first injection
 *       request.
 * </dl>
 *
 * <p>In toothpick, Scopes create a tree (actually a disjoint forest). Each scope can have children
 * scopes. Operations on the scope tree (adding / removing children, etc.) are non thread safe. The
 * implementation of Toothpick provides a {@code Toothpick} class that wraps these operations in a
 * thread safe way.
 *
 * <p>Scopes can be support scope annotations: annotation classes qualified by the {@link
 * javax.inject.Scope} annotation. All classes annotated by this annotation will automatically be
 * scoped by Toothpick in the scope that supports them.
 *
 * <p>Classes that are not annotated with a {@link javax.inject.Scope} annotation, also called
 * un-scoped classes, are not associated to a particular scope and can be used in all scopes. Their
 * instances are not recycled, every injection provides a different instance. Scoping a class by
 * annotation is conceptually exactly the same as binding it to itself in a scope.
 *
 * <p>Scope resolution : when a class is scoped, either by binding it in a module and then
 * installing this module in a scope, or by adding a {@link javax.inject.Scope} annotation, it means
 * that all its dependencies must be found in the scope itself or a parent scope. Otherwise,
 * Toothpick will crash at runtime when first instantiating this class. The only other allowed
 * alternative is to have an un-scoped dependency.
 */
public abstract class ScopeNode implements Scope {
  // lock free children. A Concurrent HashMap is better than a list here as
  // we need to know atomically which value could be already in the map
  protected final ConcurrentHashMap<Object, ScopeNode> childrenScopes = new ConcurrentHashMap<>();
  // lock free parents = each node has its own copy of the parent edges up to the root
  // it means that when we access a node, all operations are lock free relatively to
  // concurrent operations performed on the tree. There is no need for copy on write
  // as setting the parent is called only once when creating a node
  protected final List<ScopeNode> parentScopes = new CopyOnWriteArrayList<>();
  protected Object name;
  protected boolean isOpen = true;
  // same here for lock free access
  protected final Set<Class<? extends Annotation>> scopeAnnotationClasses =
      new CopyOnWriteArraySet<>();

  public ScopeNode(Object name) {
    if (name == null) {
      throw new IllegalArgumentException("A scope can't have a null name");
    }

    this.name = name;
    bindScopeAnnotationIfNameIsScopeAnnotation();
  }

  @Override
  public Object getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof Scope)) return false;

    ScopeNode scopeNode = (ScopeNode) o;

    return !(name != null ? !name.equals(scopeNode.name) : scopeNode.name != null);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /** @return the parentScope of this scope. Can be null for a root scope. */
  @Override
  public ScopeNode getParentScope() {
    Iterator<ScopeNode> parentIterator = parentScopes.iterator();
    boolean hasParent = parentIterator.hasNext();
    return hasParent ? parentIterator.next() : null;
  }

  /**
   * @param scopeAnnotationClass an annotation that should be qualified by {@link
   *     javax.inject.Scope}. If not, an exception is thrown.
   * @return the parent {@link ScopeNode} of this scope that supports {@code scopeAnnotationClass}.
   *     The current {@code scope} (this) can be returned if it, itself, supports {@code
   *     scopeAnnotationClass}. If no such parent exists, it throws an exception. This later case
   *     means that something scoped is using a lower scoped dependency, which is conceptually
   *     flawed and not allowed in Toothpick. Note that is {@code scopeAnnotationClass} is {@link
   *     Singleton}, the root scope is always returned. Thus the {@link Singleton} scope annotation
   *     class doesn't need to be supported, it's built-in.
   */
  @SuppressWarnings({"unused", "used by generated code"})
  @Override
  public <A extends Annotation> ScopeNode getParentScope(Class<A> scopeAnnotationClass) {
    checkIsAnnotationScope(scopeAnnotationClass);

    if (scopeAnnotationClass == Singleton.class) {
      return getRootScope();
    }

    ScopeNode currentScope = this;
    while (currentScope != null) {
      if (currentScope.isScopeAnnotationSupported(scopeAnnotationClass)) {
        return currentScope;
      }
      currentScope = currentScope.getParentScope();
    }
    throw new IllegalStateException(
        format(
            "There is no parent scope of %s that supports the scope scopeAnnotationClass %s", //
            this.name, //
            scopeAnnotationClass.getName()));
  }

  /**
   * @return the root scope of this scope. The root scope is the scope itself if the scope has no
   *     parent. Otherwise, if it has parents, it is the highest parent in the hierarchy of parents.
   */
  @SuppressWarnings({"unused", "used by generated code"})
  @Override
  public ScopeNode getRootScope() {
    if (parentScopes.isEmpty()) {
      return this;
    }
    return parentScopes.get(parentScopes.size() - 1);
  }

  /**
   * Binds a {@code scopeAnnotationClass}, to the current scope. The current scope will accept all
   * classes that are scoped using this {@code scopeAnnotationClass}.
   *
   * @param scopeAnnotationClass an annotation that should be qualified by {@link
   *     javax.inject.Scope}. If not, an exception is thrown. Note that the {@link Singleton} scope
   *     annotation class doesn't need to be explicitely supported, it's built-in, and supported by
   *     all root scopes (scopes without parent).
   * @see #getParentScope(Class)
   */
  @Override
  public Scope supportScopeAnnotation(Class<? extends Annotation> scopeAnnotationClass) {
    checkIsAnnotationScope(scopeAnnotationClass);
    if (scopeAnnotationClass == Singleton.class) {
      throw new IllegalArgumentException(
          format(
              "The annotation @Singleton is already supported "
                  + "by root scopes. It can't be supported programmatically."));
    }

    scopeAnnotationClasses.add(scopeAnnotationClass);
    return this;
  }

  @Override
  public boolean isScopeAnnotationSupported(Class<? extends Annotation> scopeAnnotationClass) {
    if (scopeAnnotationClass == Singleton.class) {
      return parentScopes.isEmpty();
    }

    return scopeAnnotationClasses.contains(scopeAnnotationClass);
  }

  /**
   * Resets the state of the scope. Useful for automation testing when we want to reset the scope
   * used to install test modules.
   */
  protected void reset() {
    scopeAnnotationClasses.clear();
    isOpen = true;
    bindScopeAnnotationIfNameIsScopeAnnotation();
  }

  @SuppressWarnings({"unused", "For the sake of completeness of the API."})
  Collection<ScopeNode> getChildrenScopes() {
    return childrenScopes.values();
  }

  /**
   * Adds a child {@link ScopeNode} to a {@link ScopeNode}. Children scopes have access to all
   * bindings of their parents, as well as their scoped instances, and can override them. In a lock
   * free way, this method returns the child scope : either {@code child} or a child scope that was
   * already added.
   *
   * @param child the new child scope.
   * @return either {@code child} or a child scope that was already added.
   */
  ScopeNode addChild(ScopeNode child) {
    if (child == null) {
      throw new IllegalArgumentException("Child must be non null.");
    }

    // this variable is important. It takes a snapshot of the node
    final ScopeNode parentScope = child.getParentScope();
    if (parentScope == this) {
      return child;
    }

    if (parentScope != null) {
      throw new IllegalStateException(
          format("Scope %s already has a parent: %s which is not %s", child, parentScope, this));
    }

    // non-locking allows multiple threads to arrive here,
    // we take into account the first one only
    ScopeNode scope = childrenScopes.putIfAbsent(child.getName(), child);
    if (scope != null) {
      return scope;
    }
    // this could bug in multi-thread if a node is added to 2 parents...
    // there is no atomic operation to add them both and getting sure they are the only parent
    // scopes.
    // we choose not to lock as this scenario doesn't seem meaningful
    child.parentScopes.add(this);
    child.parentScopes.addAll(parentScopes);
    return child;
  }

  void removeChild(ScopeNode child) {
    if (child == null) {
      throw new IllegalArgumentException("Child must be non null.");
    }

    // this variable is important. It takes a snapshot of the node
    final ScopeNode parentScope = child.getParentScope();
    if (parentScope == null) {
      throw new IllegalStateException(format("The scope has no parent: %s", child.getName()));
    }

    if (parentScope != this) {
      throw new IllegalStateException(
          format(
              "The scope %s has parent: different of this: %s", //
              child.getName(), parentScope.getName(), getName()));
    }

    childrenScopes.remove(child.getName());
    // make the ex-child a new root.
    child.parentScopes.clear();
  }

  void close() {
    isOpen = false;
  }

  List<Object> getParentScopesNames() {
    List<Object> parentScopesNames = new ArrayList<>();
    for (ScopeNode parentScope : parentScopes) {
      parentScopesNames.add(parentScope.getName());
    }
    return parentScopesNames;
  }

  /**
   * Bind Scope Annotation if the Scope name is a Scope Annotation. For example:
   * Toothpick.openScope(MyScopeAnnotation.class)
   */
  @SuppressWarnings("unchecked")
  private void bindScopeAnnotationIfNameIsScopeAnnotation() {
    if (name.getClass() == Class.class //
        && Annotation.class.isAssignableFrom((Class) name) //
        && isScopeAnnotationClass((Class<? extends Annotation>) name)) {
      supportScopeAnnotation((Class<? extends Annotation>) name);
    }
  }

  private void checkIsAnnotationScope(Class<? extends Annotation> scopeAnnotationClass) {
    if (!isScopeAnnotationClass(scopeAnnotationClass)) {
      throw new IllegalArgumentException(
          format(
              "The annotation %s is not a scope annotation, "
                  + "it is not qualified by javax.inject.Scope.",
              scopeAnnotationClass.getName()));
    }
  }

  private boolean isScopeAnnotationClass(Class<? extends Annotation> scopeAnnotationClass) {
    return scopeAnnotationClass.isAnnotationPresent(javax.inject.Scope.class);
  }
}
