package toothpick.compiler.memberinjector;

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
import toothpick.Injector;
import toothpick.MemberInjector;
import toothpick.compiler.CodeGenerator;
import toothpick.compiler.targets.FieldInjectionTarget;
import toothpick.compiler.targets.MethodInjectionTarget;

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
    if (fieldInjectionTargetList.isEmpty()) {
      throw new IllegalArgumentException("At least one memberInjectorInjectionTarget is needed.");
    }
  }

  public String brewJava() {
    // Interface to implement
    ClassName className = ClassName.get(targetClass);
    ParameterizedTypeName memberInjectorInterfaceParameterizedTypeName = ParameterizedTypeName.get(ClassName.get(MemberInjector.class), className);

    // Build class
    TypeSpec.Builder injectorMemberTypeSpec = TypeSpec.classBuilder(className.simpleName() + MEMBER_INJECTOR_SUFFIX)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(memberInjectorInterfaceParameterizedTypeName);
    emitSuperMemberInjectorFieldIfNeeded(injectorMemberTypeSpec);
    emitInjectMethod(injectorMemberTypeSpec, fieldInjectionTargetList, methodInjectionTargetList);

    JavaFile javaFile = JavaFile.builder(className.packageName(), injectorMemberTypeSpec.build()).build();
    return javaFile.toString();
  }

  private void emitSuperMemberInjectorFieldIfNeeded(TypeSpec.Builder injectorMemberTypeSpec) {
    if (superClassThatNeedsInjection != null) {
      ClassName superTypeThatNeedsInjection = ClassName.get(superClassThatNeedsInjection);
      ParameterizedTypeName memberInjectorSuperParameterizedTypeName =
          ParameterizedTypeName.get(ClassName.get(MemberInjector.class), superTypeThatNeedsInjection);
      FieldSpec.Builder superMemberInjectorField =
          FieldSpec.builder(memberInjectorSuperParameterizedTypeName, "superMemberInjector", Modifier.PRIVATE)
              //TODO use proper typing here
              .initializer("toothpick.registries.memberinjector.MemberInjectorRegistryLocator.getMemberInjector($L.class)",
                  superTypeThatNeedsInjection.simpleName());
      injectorMemberTypeSpec.addField(superMemberInjectorField.build());
    }
  }

  private void emitInjectMethod(TypeSpec.Builder injectorMemberTypeSpec, List<FieldInjectionTarget> fieldInjectionTargetList,
      List<MethodInjectionTarget> methodInjectionTargetList) {

    MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ClassName.get(targetClass), "target")
        .addParameter(ClassName.get(Injector.class), "injector");

    emitInjectFields(fieldInjectionTargetList, injectMethodBuilder);
    emitInjectMethods(methodInjectionTargetList, injectMethodBuilder);

    if (superClassThatNeedsInjection != null) {
      injectMethodBuilder.addStatement("superMemberInjector.inject(target, injector)");
    }

    injectorMemberTypeSpec.addMethod(injectMethodBuilder.build());
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
        injectMethodBuilder.addStatement("$T $L = injector.getInstance($T.class)", paramType, paramName, paramType);
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
    for (FieldInjectionTarget injectorInjectionTarget : fieldInjectionTargetList) {
      final String injectorGetMethodName;
      final ClassName className;
      switch (injectorInjectionTarget.kind) {
        case INSTANCE:
          injectorGetMethodName = " = injector.getInstance(";
          className = ClassName.get(injectorInjectionTarget.memberClass);
          break;
        case PROVIDER:
          injectorGetMethodName = " = injector.getProvider(";
          className = ClassName.get(injectorInjectionTarget.kindParamClass);
          break;
        case LAZY:
          injectorGetMethodName = " = injector.getLazy(";
          className = ClassName.get(injectorInjectionTarget.kindParamClass);
          break;
        case FUTURE:
          injectorGetMethodName = " = injector.getFuture(";
          className = ClassName.get(injectorInjectionTarget.kindParamClass);
          break;
        default:
          throw new IllegalStateException("The kind can't be null.");
      }
      StringBuilder assignFieldStatement;
      assignFieldStatement = new StringBuilder("target.");
      assignFieldStatement.append(injectorInjectionTarget.memberName).append(injectorGetMethodName).append(className).append(".class)");
      injectBuilder.addStatement(assignFieldStatement.toString());
    }
  }

  @Override public String getFqcn() {
    return targetClass.getQualifiedName().toString() + MEMBER_INJECTOR_SUFFIX;
  }
}
