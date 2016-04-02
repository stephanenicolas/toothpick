package toothpick.compiler.factory;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.TypeMirror;

public final class FactoryInjectionTarget {

  //TODO the injection targets should ideally not know about the annotation processing classes.
  //as we can't create them for testing
  public final List<TypeMirror> parameters = new ArrayList<>();
  public final String classPackage;
  public final String className;
  public final String targetClass;
  public final boolean hasSingletonAnnotation;
  public final boolean hasProducesSingletonAnnotation;
  //TODO better detection system, take inheritance into account.
  //TODO we need to call a hook method after injection, like {@code void @Inject init()}.
  public final boolean needsMemberInjection;

  public FactoryInjectionTarget(String classPackage, String className, String targetClass, boolean hasSingletonAnnotation,
      boolean hasProducesSingletonAnnotation, boolean needsMemberInjection) {
    this.classPackage = classPackage;
    this.className = className;
    this.targetClass = targetClass;
    this.hasSingletonAnnotation = hasSingletonAnnotation;
    this.hasProducesSingletonAnnotation = hasProducesSingletonAnnotation;
    this.needsMemberInjection = needsMemberInjection;
  }

  public String getFqcn() {
    return classPackage + "." + className;
  }
}
