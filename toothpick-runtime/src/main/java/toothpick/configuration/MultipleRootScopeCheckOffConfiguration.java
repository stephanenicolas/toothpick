package toothpick.configuration;

import toothpick.Scope;

/**
 * Dummy implementation of the {@link MultipleRootScopeCheckConfiguration} strategy.
 */
class MultipleRootScopeCheckOffConfiguration implements MultipleRootScopeCheckConfiguration {
  @Override public void checkMultipleRootScopes(Scope scope) {
  }

  @Override public void onScopeForestReset() {

  }
}
