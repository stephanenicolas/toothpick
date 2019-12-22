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

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import toothpick.Scope.ScopeConfig;
import toothpick.configuration.Configuration;
import toothpick.configuration.ConfigurationHolder;

/**
 * Main class to access toothpick features. It allows to create / retrieve scopes and perform
 * injections.
 *
 * <p>The main rule about using TP is : <b>TP will honor all injections in the instances it creates
 * by itself.</b><br>
 * <em/>A soon as you use {@code new Foo}, in a provider or a binding for instance, TP is not
 * responsible for injecting Foo; developers have to manually inject the instances they create.</em>
 * <br/
 */
public class Toothpick {

  // TP must be lock free, any thread can see the state of tp before or after it is transformed but
  // not during
  // its transformation
  private static final ConcurrentHashMap<Object, Scope> MAP_KEY_TO_SCOPE =
      new ConcurrentHashMap<>();
  // ConcurrentHashSet doesn't exist so we're not using a Set ;)
  private static final ConcurrentHashMap<Object, Scope> ROOT_SCOPES = new ConcurrentHashMap<>();
  static Injector injector = new InjectorImpl();

  protected Toothpick() {
    throw new RuntimeException("Constructor can't be invoked even via reflection.");
  }

  /**
   * Creates or opens the previously created root scope as follows:<br>
   *
   * <ul>
   *   <li>if there's an existing scope tree the root scope of that tree;
   *   <li>creates a default root scope if there's none;
   *   <li>throws an exception if there are multiple scope trees (e.g. a forest);
   * </ul>
   *
   * @return the root scope.
   */
  public static Scope openRootScope() {
    synchronized (ROOT_SCOPES) {
      if (ROOT_SCOPES.size() > 1) {
        throw new RuntimeException(
            "openRootScope() is not supported when multiple root scopes are enabled. Use 'Configuration.preventMultipleRootScopes()' to enable it.");
      } else if (ROOT_SCOPES.size() == 1) {
        return ROOT_SCOPES.values().iterator().next();
      }
      return openScope(Toothpick.class);
    }
  }

  /**
   * Creates or opens the previously created root scope as follows:<br>
   *
   * <ul>
   *   <li>if there's an existing scope tree the root scope of that tree;
   *   <li>creates a default root scope if there's none; In this case, {@code scopeConfig} is
   *       applied to the new scope.
   *   <li>throws an exception if there are multiple scope trees (e.g. a forest);
   * </ul>
   *
   * @param scopeConfig a lambda to configure the scope if it is created. The lambda is not applied
   *     if the scope existed already.
   * @return the root scope.
   */
  public static Scope openRootScope(ScopeConfig scopeConfig) {
    if (isRootScopeOpen()) {
      return openRootScope();
    }
    Scope scope = openRootScope();
    scopeConfig.configure(scope);
    return scope;
  }

  /**
   * Indicates whether the root scope is open.
   *
   * @return true if the root scope has been opened and not yet closed.
   */
  public static boolean isRootScopeOpen() {
    return !ROOT_SCOPES.isEmpty();
  }

  /**
   * Indicates whether a scope is open.
   *
   * @param name the name of the scope.
   * @return true if the scope has been opened and not yet closed.
   */
  public static boolean isScopeOpen(Object name) {
    if (name == null) {
      throw new IllegalArgumentException("null scope names are not allowed.");
    }

    return MAP_KEY_TO_SCOPE.containsKey(name);
  }

  /**
   * Opens multiple scopes in a row. Opened scopes will be children of each other in left to right
   * order (e.g. {@code openScopes(a,b)} opens scopes {@code a} and {@code b} and {@code b} is a
   * child of {@code a}.
   *
   * @param names of the scopes to open hierarchically.
   * @return the last opened scope, leaf node of the created subtree of scopes.
   */
  public static Scope openScopes(Object... names) {

    if (names == null) {
      throw new IllegalArgumentException("null scope names are not allowed.");
    }

    if (names.length == 0) {
      throw new IllegalArgumentException("Minimally, one scope name is required.");
    }

    ScopeNode lastScope = null;
    ScopeNode previousScope = (ScopeNode) openScope(names[0], true);
    for (int i = 1; i < names.length; i++) {
      lastScope = (ScopeNode) openScope(names[i], false);
      lastScope = previousScope.addChild(lastScope);
      previousScope = lastScope;
    }

    return previousScope;
  }

  /**
   * Opens a scope without any parent. If a scope by this {@code name} already exists, it is
   * returned. Otherwise a new scope is created.
   *
   * @param name the <em>name of the scope</em>.
   * @see #openScopes(Object...)
   * @see #openScope(Object, ScopeConfig)
   * @see #closeScope(Object)
   */
  public static Scope openScope(Object name) {
    return openScope(name, true);
  }

  /**
   * Opens a scope without any parent. If a scope by this {@code name} already exists, it is
   * returned. Otherwise a new scope is created. If a new scope is created, then {@code scopeConfig}
   * is applied to the new scope.
   *
   * @param name the <em>name of the scope</em>.
   * @param scopeConfig a lambda to configure the scope if it is created. The lambda is not applied
   *     if the scope existed already.
   * @see #openScopes(Object...)
   * @see #openScope(Object)
   * @see #closeScope(Object)
   */
  public static Scope openScope(Object name, ScopeConfig scopeConfig) {
    if (isScopeOpen(name)) {
      return openScope(name);
    }
    Scope scope = openScope(name, true);
    scopeConfig.configure(scope);
    return scope;
  }

  /**
   * Opens a scope without any parent. If a scope by this {@code name} already exists, it is
   * returned. Otherwise a new scope is created.
   *
   * @param name the name of the scope to open.
   * @param isRootScope whether or not this is a root scope
   */
  private static Scope openScope(Object name, boolean isRootScope) {
    synchronized (ROOT_SCOPES) {
      if (name == null) {
        throw new IllegalArgumentException("null scope names are not allowed.");
      }

      Scope scope = MAP_KEY_TO_SCOPE.get(name);
      if (scope != null) {
        return scope;
      }
      scope = new ScopeImpl(name);
      Scope previous = MAP_KEY_TO_SCOPE.putIfAbsent(name, scope);
      if (previous != null) {
        // if there was already a scope by this name, we return it
        scope = previous;
      } else if (isRootScope) {
        ROOT_SCOPES.put(name, scope);
        ConfigurationHolder.configuration.checkMultipleRootScopes(scope);
      }
      return scope;
    }
  }

  /**
   * Detach a scope from its parent, this will trigger the garbage collection of this scope and it's
   * sub-scopes if they are not referenced outside of Toothpick.
   *
   * @param name the name of the scope to close.
   */
  public static void closeScope(Object name) {
    synchronized (ROOT_SCOPES) {
      // we remove the scope first, so that other threads don't see it, and see the next snapshot of
      // the tree
      ScopeNode scope = (ScopeNode) MAP_KEY_TO_SCOPE.remove(name);
      if (scope != null) {
        ScopeNode parentScope = scope.getParentScope();
        if (parentScope != null) {
          parentScope.removeChild(scope);
        } else {
          ConfigurationHolder.configuration.onScopeForestReset();
          ROOT_SCOPES.remove(name);
        }
        removeScopeAndChildrenFromMap(scope);
      }
    }
  }

  /** Clears all scopes. Useful for testing and not getting any leak... */
  public static void reset() {
    for (Object name : Collections.list(MAP_KEY_TO_SCOPE.keys())) {
      closeScope(name);
    }
    ConfigurationHolder.configuration.onScopeForestReset();
    ScopeImpl.resetUnScopedProviders();
  }

  /**
   * Resets the state of a single scope. Useful for automation testing when we want to reset the
   * scope used to install test modules.
   *
   * @param scope the scope we want to reset.
   */
  public static void reset(Scope scope) {
    ScopeNode scopeNode = (ScopeNode) scope;
    scopeNode.reset();
  }

  /**
   * Resets the state of a single scope. Useful for automation testing when we want to reset the
   * scope used to install test modules.
   *
   * @param scope the scope we want to reset.
   */
  public static void release(Scope scope) {
    ScopeNode scopeNode = (ScopeNode) scope;
    scopeNode.release();
  }

  /**
   * Injects all dependencies (transitively) in {@code obj}, dependencies will be obtained in the
   * scope {@code scope}.
   *
   * @param obj the object to be injected.
   * @param scope the scope in which all dependencies are obtained.
   */
  public static void inject(Object obj, Scope scope) {
    injector.inject(obj, scope);
  }

  /**
   * Removes all nodes of {@code scope} using DFS. We don't lock here.
   *
   * @param scope the parent scope of which all children will recursively be removed from the map.
   *     We don't do anything else to the children nodes are they will be garbage collected soon. We
   *     just cut a whole sub-graph in the references graph of the JVM normally.
   */
  private static void removeScopeAndChildrenFromMap(ScopeNode scope) {
    MAP_KEY_TO_SCOPE.remove(scope.getName());
    scope.close();
    for (ScopeNode childScope : scope.childrenScopes.values()) {
      removeScopeAndChildrenFromMap(childScope);
    }
  }

  /**
   * Allows to pass custom configurations.
   *
   * @param configuration the configuration to use
   */
  public static void setConfiguration(Configuration configuration) {
    ConfigurationHolder.configuration = configuration;
  }

  /*for testing.*/
  static int getScopeNamesSize() {
    return MAP_KEY_TO_SCOPE.size();
  }
}
