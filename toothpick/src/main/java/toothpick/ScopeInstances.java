package toothpick;

import javax.inject.Singleton;

/**
 * Can only be used to annotate {@link Provider} classes (TODO check this).
 * Indicates that the provider will create a singleton in a scope.
 * The scope must be provided as a different annotation of the same type.
 *
 * It is different from annotating the provider class with  a scope annotation such as {@link Singleton}. In that
 * case, the provider itself would be a singleton, but it tells nothing about the instances
 * provided by the provider.
 *
 * Technically, the provider and the provided instances will all be in the same scope
 * defined by a different annotation. The provider is also created in the same scope.
 */
public @interface ScopeInstances {
}
