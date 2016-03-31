package toothpick.compiler.memberinjector;

public class MemberInjectorGenerator {

  private static final String MemberInjector_SUFFIX = "$$MemberInjector";

  private MemberInjectorInjectionTarget MemberInjectorInjectionTarget;

  public MemberInjectorGenerator(MemberInjectorInjectionTarget MemberInjectorInjectionTarget) {
    this.MemberInjectorInjectionTarget = MemberInjectorInjectionTarget;
  }

  public String brewJava() {
    /*TypeSpec.Builder injectorTypeSpec =
        TypeSpec.classBuilder(injectionTarget.className + MemberInjector_SUFFIX)
            .addModifiers(Modifier.PUBLIC);
    emitInject(injectorTypeSpec);
    JavaFile javaFile = JavaFile.builder(target.classPackage, injectorTypeSpec.build())
        .addFileComment("Generated code from Dart. Do not modify!")
        .build();
    return javaFile.toString();*/
    return "";
  }

  public String getFqcn() {
    return MemberInjectorInjectionTarget.getFqcn() + MemberInjector_SUFFIX;
  }
}
