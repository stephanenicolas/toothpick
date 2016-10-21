package toothpick.configuration;

import toothpick.Scope;

/**
 * Checks that TP scope forest is actually a tree.
 * If a new created scope is a new root outside of the first created tree,
 * it will throw a {@link MultipleRootException}.
 */
class MultipleRootScopeCheckOnConfiguration implements MultipleRootScopeCheckConfiguration {
  private Scope rootScope;

  @Override public synchronized void checkMultipleRootScopes(Scope scope) {
    if (rootScope == null && scope != null) {
      rootScope = scope;
      return;
    }

    if (scope == rootScope) {
      return;
    }

    if (scope.getParentScope() != null) {
      return;
    }

    throw new MultipleRootException(scope);
  }

  @Override public synchronized void onScopeForestReset() {
    rootScope = null;
  }
}
