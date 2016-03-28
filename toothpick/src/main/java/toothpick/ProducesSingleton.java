package toothpick;

import javax.inject.Singleton;

/**
 * Can only be used to annotate {@link Provider} classes.
 * Indicates that the producer will create a singleton.
 *
 * It is different from annotating the provider class with {@link Singleton}. In that
 * case, the provider itself would be a singleton, but it tells nothing about the instances
 * provided by the provider.
 */
public @interface ProducesSingleton {
}
