package toothpick;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to designate a scope by name when declaring classes.
 */
@javax.inject.Scope
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scoped {
  /** The name. */
  String value() default "";
}
