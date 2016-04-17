package toothpick.compiler.memberinjector.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import toothpick.MemberInjector;
import toothpick.Scope;
import toothpick.compiler.CodeGenerator;
import toothpick.compiler.CodeGeneratorUtil;
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget;
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget;

/**
 * Generates a {@link MemberInjector} for a given collection of {@link FieldInjectionTarget}.
 * Typically a {@link MemberInjector} is created for a class a soon as it contains
 * an {@link javax.inject.Inject} annotated field.
 * TODO also deal with injected methods.
 */
public class MemberInjectorGenerator implements CodeGenerator {

  private static final String MEMBER_INJECTOR_SUFFIX = "$$MemberInjector";

  private TypeElement targetClass;
  private TypeElement superClassThatNeedsInjection;
  private List<FieldInjectionTarget> fieldInjectionTargetList;
  private List<MethodInjectionTarget> methodInjectionTargetList;

  public MemberInjectorGenerator(TypeElement targetClass, TypeElement superClassThatNeedsInjection,
      List<FieldInjectionTarget> fieldInjectionTargetList, List<MethodInjectionTarget> methodInjectionTargetList) {
    this.targetClass = targetClass;
    this.superClassThatNeedsInjection = superClassThatNeedsInjection;
    this.fieldInjectionTargetList = fieldInjectionTargetList;
    this.methodInjectionTargetList = methodInjectionTargetList;
    if (fieldInjectionTargetList == null && methodInjectionTargetList == null) {
      throw new IllegalArgumentException("At least one memberInjectorInjectionTarget is needed.");
    }
  }

  public String brewJava() {
    // Interface to implement
    ClassName className = ClassName.get(targetClass);
    ParameterizedTypeName memberInjectorInterfaceParameterizedTypeName = ParameterizedTypeName.get(ClassName.get(MemberInjector.class), className);

    // Build class
    TypeSpec.Builder scopeMemberTypeSpec = TypeSpec.classBuilder(CodeGeneratorUtil.getGeneratedSimpleClassName(targetClass) + MEMBER_INJECTOR_SUFFIX)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(memberInjectorInterfaceParameterizedTypeName); emitSuperMemberInjectorFieldIfNeeded(scopeMemberTypeSpec);
    emitInjectMethod(scopeMemberTypeSpec, fieldInjectionTargetList, methodInjectionTargetList);

    JavaFile javaFile = JavaFile.builder(className.packageName(), scopeMemberTypeSpec.build()).build();
    return javaFile.toString();
  }

  private void emitSuperMemberInjectorFieldIfNeeded(TypeSpec.Builder scopeMemberTypeSpec) {
    if (superClassThatNeedsInjection != null) {
      ClassName superTypeThatNeedsInjection = ClassName.get(superClassThatNeedsInjection);
      ParameterizedTypeName memberInjectorSuperParameterizedTypeName =
          ParameterizedTypeName.get(ClassName.get(MemberInjector.class), superTypeThatNeedsInjection);
      FieldSpec.Builder superMemberInjectorField =
          FieldSpec.builder(memberInjectorSuperParameterizedTypeName, "superMemberInjector", Modifier.PRIVATE)
              //TODO use proper typing here
              .initializer("new $L$$$$MemberInjector()", superTypeThatNeedsInjection);
      scopeMemberTypeSpec.addField(superMemberInjectorField.build());
    }
  }

  private void emitInjectMethod(TypeSpec.Builder scopeMemberTypeSpec, List<FieldInjectionTarget> fieldInjectionTargetList,
      List<MethodInjectionTarget> methodInjectionTargetList) {

    MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(targetClass), "target")
        .addParameter(ClassName.get(Scope.class), "scope");

    emitInjectFields(fieldInjectionTargetList, injectMethodBuilder);
    emitInjectMethods(methodInjectionTargetList, injectMethodBuilder);

    if (superClassThatNeedsInjection != null) {
      injectMethodBuilder.addStatement("superMemberInjector.inject(target, scope)");
    }

    scopeMemberTypeSpec.addMethod(injectMethodBuilder.build());
  }

  private void emitInjectMethods(List<MethodInjectionTarget> methodInjectionTargetList, MethodSpec.Builder injectMethodBuilder) {
    if (methodInjectionTargetList == null) {
      return;
    }
    int counter = 1;
    for (MethodInjectionTarget methodInjectionTarget : methodInjectionTargetList) {

      StringBuilder injectedMethodCallStatement = new StringBuilder();
      injectedMethodCallStatement.append("target.");
      injectedMethodCallStatement.append(methodInjectionTarget.methodName);
      injectedMethodCallStatement.append("(");
      String prefix = "";

      for (TypeMirror typeMirror : methodInjectionTarget.parameters) {
        String paramName = "param" + counter++;
        TypeName paramType = TypeName.get(typeMirror);
        injectMethodBuilder.addStatement("$T $L = scope.getInstance($L.class)", paramType, paramName, paramType);
        injectedMethodCallStatement.append(prefix);
        injectedMethodCallStatement.append(paramName);
        prefix = ", ";
      }
      injectedMethodCallStatement.append(")");

      injectMethodBuilder.addStatement(injectedMethodCallStatement.toString());
    }
  }

  private void emitInjectFields(List<FieldInjectionTarget> fieldInjectionTargetList, MethodSpec.Builder injectBuilder) {
    if (fieldInjectionTargetList == null) {
      return;
    }
    for (FieldInjectionTarget memberInjectionTarget : fieldInjectionTargetList) {
      final String scopeGetMethodName;
      final String injectionName;
      if (memberInjectionTarget.name == null) {
        injectionName = "";
      } else {
        injectionName = ", \"" + memberInjectionTarget.name.toString() + "\"";
      }
      final ClassName className;
      switch (memberInjectionTarget.kind) {
        case INSTANCE:
          scopeGetMethodName = "getInstance";
          className = ClassName.get(memberInjectionTarget.memberClass);
          break;
        case PROVIDER:
          scopeGetMethodName = "getProvider";
          className = ClassName.get(memberInjectionTarget.kindParamClass);
          break;
        case LAZY:
          scopeGetMethodName = "getLazy";
          className = ClassName.get(memberInjectionTarget.kindParamClass);
          break;
        default:
          throw new IllegalStateException("The kind can't be null.");
      }
      injectBuilder.addStatement("target.$L = scope.$L($T.class$L)", memberInjectionTarget.memberName, scopeGetMethodName, className, injectionName);
    }
  }

  @Override
  public String getFqcn() {
    return CodeGeneratorUtil.getGeneratedFQNClassName(targetClass) + MEMBER_INJECTOR_SUFFIX;

  }
}
