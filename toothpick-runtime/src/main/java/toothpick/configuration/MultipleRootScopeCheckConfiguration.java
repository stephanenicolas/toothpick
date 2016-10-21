package toothpick.configuration;

import toothpick.Scope;

/**
 * Check strategy to detect when mutiple roots
 * are created in TP scope forest.
 */
interface MultipleRootScopeCheckConfiguration {
  /**
   * Check that a scope doesn't introduce a second root
   * in TP scope forest.
   *
   * @param scope a newly created scope.
   */
  void checkMultipleRootScopes(Scope scope);

  /**
   * Reset the state of the detector.
   */
  void onScopeForestReset();
}
