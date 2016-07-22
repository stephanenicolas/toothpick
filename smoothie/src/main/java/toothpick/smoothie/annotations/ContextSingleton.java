package toothpick.smoothie.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Identifies a type that the injector only instantiates once
 * per context. This scope annotation has to be bound to
 * service &amp; activity scopes. Not inherited.
 *
 * @see javax.inject.Scope @Scope
 */
@Scope
@Documented
@Retention(CLASS)
public @interface ContextSingleton {
}
