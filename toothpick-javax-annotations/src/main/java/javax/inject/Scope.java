//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package javax.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE})
//note: this retention HAS TO REMAIN RUNTIME
//otherwise it creates issues when binding a scope annotation
//at runtime
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {
}
