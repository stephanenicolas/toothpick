package toothpick.compiler.memberinjector;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import toothpick.Factory;
import toothpick.Injector;
import toothpick.MemberInjector;

public class MemberInjectorGenerator {

  private static final String MEMBER_INJECTOR_SUFFIX = "$$MemberInjector";

  private List<MemberInjectorInjectionTarget> memberInjectorInjectionTargetList;

  public MemberInjectorGenerator(List<MemberInjectorInjectionTarget> memberInjectorInjectionTargetList) {
    this.memberInjectorInjectionTargetList = memberInjectorInjectionTargetList;
    if(memberInjectorInjectionTargetList.size() < 1) {
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
    MethodSpec.Builder createInstanceBuilder = MethodSpec.methodBuilder("inject")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(memberInjectorInjectionTarget.targetClassPackage, memberInjectorInjectionTarget.targetClassName), "t")
        .addParameter(ClassName.get(Injector.class), "injector");

    /*
    StringBuilder localVarStatement = new StringBuilder("");
    localVarStatement.append(factoryInjectionTarget.className).append(" ");
    String varName = "" + Character.toLowerCase(factoryInjectionTarget.className.charAt(0));
    varName += factoryInjectionTarget.className.substring(1);
    localVarStatement.append(varName).append(" = ");
    localVarStatement.append("new ");
    localVarStatement.append(factoryInjectionTarget.className).append("(");
    int counter = 1;
    String prefix = "";

    for (TypeMirror typeMirror : factoryInjectionTarget.parameters) {
      String paramName = "param" + counter++;
      TypeName paramType = TypeName.get(typeMirror);
      createInstanceBuilder.addStatement("$T $L = injector.getInstance($T.class)", paramType, paramName, paramType);
      localVarStatement.append(prefix);
      localVarStatement.append(paramName);
      prefix = ", ";
    }

    localVarStatement.append(")");
    createInstanceBuilder.addStatement(localVarStatement.toString());
    if (factoryInjectionTarget.needsMemberInjection) {
      StringBuilder injectStatement = new StringBuilder("injector.inject(");
      injectStatement.append(varName);
      injectStatement.append(")");
      createInstanceBuilder.addStatement(injectStatement.toString());
    }
    StringBuilder returnStatement = new StringBuilder("return ");
    returnStatement.append(varName);
    createInstanceBuilder.addStatement(returnStatement.toString());
*/
    builder.addMethod(createInstanceBuilder.build());
  }


  public String getFqcn() {
    MemberInjectorInjectionTarget firstMemberInjector = memberInjectorInjectionTargetList.get(0);
    return  firstMemberInjector.targetClassPackage + "." + firstMemberInjector.targetClassName + MEMBER_INJECTOR_SUFFIX;
  }
}
