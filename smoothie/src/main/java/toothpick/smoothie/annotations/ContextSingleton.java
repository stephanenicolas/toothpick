package toothpick.smoothie.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a type that the injector only instantiates once
 * per context. This scope annotation has to be bound to
 * service &amp; activity scopes. Not inherited.
 *
 * Note that this annotation is just provided to ease the migration
 * from RG to TP. We highly recommend not keeping it after the migration
 * is complete and to clearly identify what you inject: either the application context
 * or an activity (or a service). Using this scope annotation and injection contexts blindly
 * is a perfect recipe for memory leaks.
 *
 * @see javax.inject.Scope @Scope
 * Deprecated, do not inject contexts blindly, this is a bad practice. Read above.
 */
@Scope
@Documented
@Retention(RUNTIME)
@Deprecated
public @interface ContextSingleton {
}
