package toothpick;

public class FactoryGenerator {

  private static final String FACTORY_SUFFIX = "$$Factory";

  private InjectionTarget injectionTarget;

  public FactoryGenerator(InjectionTarget injectionTarget) {
    this.injectionTarget = injectionTarget;
  }

  public String brewJava() {
    /*TypeSpec.Builder injectorTypeSpec =
        TypeSpec.classBuilder(injectionTarget.className + FACTORY_SUFFIX)
            .addModifiers(Modifier.PUBLIC);
    emitInject(injectorTypeSpec);
    JavaFile javaFile = JavaFile.builder(target.classPackage, injectorTypeSpec.build())
        .addFileComment("Generated code from Dart. Do not modify!")
        .build();
    return javaFile.toString();*/
    return "";
  }

  public String getFqcn() {
    return injectionTarget.getFqcn() + FACTORY_SUFFIX;
  }
}
