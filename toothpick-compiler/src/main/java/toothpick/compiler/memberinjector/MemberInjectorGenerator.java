package toothpick.compiler.memberinjector;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.lang.model.element.Modifier;
import toothpick.Injector;
import toothpick.MemberInjector;

public class MemberInjectorGenerator {

  private static final String MEMBER_INJECTOR_SUFFIX = "$$MemberInjector";

  private List<MemberInjectorInjectionTarget> memberInjectorInjectionTargetList;

  public MemberInjectorGenerator(List<MemberInjectorInjectionTarget> memberInjectorInjectionTargetList) {
    this.memberInjectorInjectionTargetList = memberInjectorInjectionTargetList;
    if (memberInjectorInjectionTargetList.size() < 1) {
      throw new IllegalStateException("At least one memberInjectorInjectionTarget is needed.");
    }
  }

  public String brewJava() {
    // Interface to implement
    MemberInjectorInjectionTarget memberInjectorInjectionTarget = memberInjectorInjectionTargetList.get(0);
    ClassName className = ClassName.get(memberInjectorInjectionTarget.targetClassPackage, memberInjectorInjectionTarget.targetClassName);
    ParameterizedTypeName memberInjectorInterfaceParameterizedTypeName = ParameterizedTypeName.get(ClassName.get(MemberInjector.class), className);

    // Build class
    TypeSpec.Builder factoryTypeSpec = TypeSpec.classBuilder(memberInjectorInjectionTarget.targetClassName + MEMBER_INJECTOR_SUFFIX)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(memberInjectorInterfaceParameterizedTypeName);
    emitInjectMethod(factoryTypeSpec, memberInjectorInjectionTargetList);

    JavaFile javaFile = JavaFile.builder(memberInjectorInjectionTarget.targetClassPackage, factoryTypeSpec.build())
        .addFileComment("Generated code from ToothPick. Do not modify!")
        .build();
    return javaFile.toString();
  }

  private void emitInjectMethod(TypeSpec.Builder builder, List<MemberInjectorInjectionTarget> memberInjectorInjectionTargetList) {
    MemberInjectorInjectionTarget memberInjectorInjectionTarget = memberInjectorInjectionTargetList.get(0);
    MethodSpec.Builder injectBuilder = MethodSpec.methodBuilder("inject")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(memberInjectorInjectionTarget.targetClassPackage, memberInjectorInjectionTarget.targetClassName), "t")
        .addParameter(ClassName.get(Injector.class), "injector");

    for (MemberInjectorInjectionTarget injectorInjectionTarget : memberInjectorInjectionTargetList) {
      String varName = "" + Character.toLowerCase(injectorInjectionTarget.memberClassName.charAt(0));
      varName += injectorInjectionTarget.memberClassName.substring(1);
      StringBuilder assignFieldStatement = new StringBuilder("t.");
      assignFieldStatement.append(varName)
          .append(" = injector.getInstance(")
          .append(ClassName.get(injectorInjectionTarget.memberClassPackage, injectorInjectionTarget.memberClassName))
          .append(".class)");
      injectBuilder.addStatement(assignFieldStatement.toString());
    }
    builder.addMethod(injectBuilder.build());
  }

  public String getFqcn() {
    MemberInjectorInjectionTarget firstMemberInjector = memberInjectorInjectionTargetList.get(0);
    return firstMemberInjector.targetClassPackage + "." + firstMemberInjector.targetClassName + MEMBER_INJECTOR_SUFFIX;
  }
}
