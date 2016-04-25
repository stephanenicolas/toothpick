package toothpick;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to designate a scope by name when declaring implementation or provider classes.
 * {@code ""} designates the current scope. Otherwise all scope annotations can be referred to
 * by using their fully qualified name (e.g. {@code @Scoped("example.Foo"}) where {@code example.Foo} class is {@code @Scope @interface Foo}.
 * Not that the {@code javax.singleton.Singleton} is an exception, it can be accessed via {@code @Scoped}.
 */
@javax.inject.Scope
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scoped {
  /** The name of the scope. */
  String value() default "";
}
