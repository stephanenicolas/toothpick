package toothpick.compiler.memberinjector;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import toothpick.MemberInjector;
import toothpick.compiler.CodeGenerator;
import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;

public class MemberInjectorRegistryGenerator implements CodeGenerator {

  private MemberInjectorRegistryInjectionTarget memberInjectorRegistryInjectionTarget;

  public MemberInjectorRegistryGenerator(MemberInjectorRegistryInjectionTarget memberInjectorRegistryInjectionTarget) {
    this.memberInjectorRegistryInjectionTarget = memberInjectorRegistryInjectionTarget;
  }

  public String brewJava() {
    // Build class
    TypeSpec.Builder memberInjectorRegistryTypeSpec =
        TypeSpec.classBuilder(MemberInjectorRegistryInjectionTarget.MEMBER_INJECTOR_REGISTRY_NAME)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            //TODO do not use the class but a name, this ties the generator to the AbstractFactoryRegistry
            //and forces up to put it in the toothpick lib vs runtime lib, which is not desirable
            //the runtime package could still be used for running tests..
            .superclass(ClassName.get(AbstractMemberInjectorRegistry.class));

    emitConstructor(memberInjectorRegistryTypeSpec);
    emitGetMemberInjectorMethod(memberInjectorRegistryTypeSpec);

    JavaFile javaFile = JavaFile.builder(memberInjectorRegistryInjectionTarget.packageName, memberInjectorRegistryTypeSpec.build())
        .addFileComment("Generated code from Toothpick. Do not modify!")
        .build();
    return javaFile.toString();
  }

  private void emitConstructor(TypeSpec.Builder memberInjectorRegistryTypeSpec) {
    MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    CodeBlock.Builder iterateChildAddMemberInjectorRegistryBlock = CodeBlock.builder();
    for (String childPackageName : memberInjectorRegistryInjectionTarget.childrenRegistryPackageNameList) {
      ClassName memberInjectorRegistryClassName =
          ClassName.get(childPackageName, MemberInjectorRegistryInjectionTarget.MEMBER_INJECTOR_REGISTRY_NAME);
      iterateChildAddMemberInjectorRegistryBlock.addStatement("addChildRegistry($L)", memberInjectorRegistryClassName);
    }

    constructor.addCode(iterateChildAddMemberInjectorRegistryBlock.build());
    memberInjectorRegistryTypeSpec.addMethod(constructor.build());
  }

  private void emitGetMemberInjectorMethod(TypeSpec.Builder memberInjectorRegistryTypeSpec) {
    TypeVariableName t = TypeVariableName.get("T");
    MethodSpec.Builder getMemberInjectorMethod = MethodSpec.methodBuilder("getMemberInjector")
        .addTypeVariable(t)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), t), "clazz")
        .returns(ParameterizedTypeName.get(ClassName.get(MemberInjector.class), t));

    CodeBlock.Builder switchBlockBuilder = CodeBlock.builder().beginControlFlow("switch($L)", "clazz.getName()");

    for (TypeElement typeElement : memberInjectorRegistryInjectionTarget.memberInjectionTargetList) {
      switchBlockBuilder.add("case ($S):\n", typeElement.getQualifiedName().toString());
      switchBlockBuilder.addStatement("return (MemberInjector<T>) new $L$$$$MemberInjector()", ClassName.get(typeElement));
    }
    switchBlockBuilder.add("default:\n");
    switchBlockBuilder.addStatement("return getMemberInjectorInChildrenRegistries(clazz)");
    switchBlockBuilder.endControlFlow();
    getMemberInjectorMethod.addCode(switchBlockBuilder.build());
    memberInjectorRegistryTypeSpec.addMethod(getMemberInjectorMethod.build());
  }

  public String getFqcn() {
    return memberInjectorRegistryInjectionTarget.getFqcn();
  }
}
