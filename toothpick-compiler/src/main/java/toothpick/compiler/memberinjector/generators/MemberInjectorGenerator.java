/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.compiler.memberinjector.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import toothpick.MemberInjector;
import toothpick.Scope;
import toothpick.compiler.common.generators.CodeGenerator;
import toothpick.compiler.common.generators.targets.ParamInjectionTarget;
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget;
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget;

/**
 * Generates a {@link MemberInjector} for a given collection of {@link FieldInjectionTarget}.
 * Typically a {@link MemberInjector} is created for a class a soon as it contains an {@link
 * javax.inject.Inject} annotated field or method.
 */
public class MemberInjectorGenerator extends CodeGenerator {

  private static final String MEMBER_INJECTOR_SUFFIX = "__MemberInjector";

  private TypeElement targetClass;
  private TypeElement superClassThatNeedsInjection;
  private List<FieldInjectionTarget> fieldInjectionTargetList;
  private List<MethodInjectionTarget> methodInjectionTargetList;

  public MemberInjectorGenerator(
      TypeElement targetClass,
      TypeElement superClassThatNeedsInjection,
      List<FieldInjectionTarget> fieldInjectionTargetList,
      List<MethodInjectionTarget> methodInjectionTargetList,
      Types types) {
    super(types);
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
    ParameterizedTypeName memberInjectorInterfaceParameterizedTypeName =
        ParameterizedTypeName.get(ClassName.get(MemberInjector.class), className);

    // Build class
    TypeSpec.Builder scopeMemberTypeSpec =
        TypeSpec.classBuilder(getGeneratedSimpleClassName(targetClass) + MEMBER_INJECTOR_SUFFIX)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(memberInjectorInterfaceParameterizedTypeName);
    emitSuperMemberInjectorFieldIfNeeded(scopeMemberTypeSpec);
    emitInjectMethod(scopeMemberTypeSpec, fieldInjectionTargetList, methodInjectionTargetList);

    JavaFile javaFile =
        JavaFile.builder(className.packageName(), scopeMemberTypeSpec.build()).build();
    return javaFile.toString();
  }

  private void emitSuperMemberInjectorFieldIfNeeded(TypeSpec.Builder scopeMemberTypeSpec) {
    if (superClassThatNeedsInjection != null) {
      FieldSpec.Builder superMemberInjectorField =
          FieldSpec.builder(
                  ParameterizedTypeName.get(
                      ClassName.get(MemberInjector.class),
                      TypeName.get(typeUtil.erasure(superClassThatNeedsInjection.asType()))),
                  "superMemberInjector",
                  Modifier.PRIVATE)
              // TODO use proper typing here
              .initializer(
                  "new $L__MemberInjector()",
                  getGeneratedFQNClassName(superClassThatNeedsInjection));
      scopeMemberTypeSpec.addField(superMemberInjectorField.build());
    }
  }

  private void emitInjectMethod(
      TypeSpec.Builder scopeMemberTypeSpec,
      List<FieldInjectionTarget> fieldInjectionTargetList,
      List<MethodInjectionTarget> methodInjectionTargetList) {

    MethodSpec.Builder injectMethodBuilder =
        MethodSpec.methodBuilder("inject")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.get(targetClass), "target")
            .addParameter(ClassName.get(Scope.class), "scope");

    if (superClassThatNeedsInjection != null) {
      injectMethodBuilder.addStatement("superMemberInjector.inject(target, scope)");
    }
    emitInjectFields(fieldInjectionTargetList, injectMethodBuilder);
    emitInjectMethods(methodInjectionTargetList, injectMethodBuilder);

    scopeMemberTypeSpec.addMethod(injectMethodBuilder.build());
  }

  private void emitInjectMethods(
      List<MethodInjectionTarget> methodInjectionTargetList,
      MethodSpec.Builder injectMethodBuilder) {
    if (methodInjectionTargetList == null) {
      return;
    }
    int counter = 1;
    for (MethodInjectionTarget methodInjectionTarget : methodInjectionTargetList) {

      if (methodInjectionTarget.isOverride) {
        continue;
      }
      StringBuilder injectedMethodCallStatement = new StringBuilder();
      injectedMethodCallStatement.append("target.");
      injectedMethodCallStatement.append(methodInjectionTarget.methodName);
      injectedMethodCallStatement.append("(");
      String prefix = "";

      for (ParamInjectionTarget paramInjectionTarget : methodInjectionTarget.parameters) {
        CodeBlock invokeScopeGetMethodWithNameCodeBlock =
            getInvokeScopeGetMethodWithNameCodeBlock(paramInjectionTarget);
        String paramName = "param" + counter++;
        injectMethodBuilder.addCode(
            "$T $L = scope.", getParamType(paramInjectionTarget), paramName);
        injectMethodBuilder.addCode(invokeScopeGetMethodWithNameCodeBlock);
        injectMethodBuilder.addCode(";");
        injectMethodBuilder.addCode(LINE_SEPARATOR);
        injectedMethodCallStatement.append(prefix);
        injectedMethodCallStatement.append(paramName);
        prefix = ", ";
      }
      injectedMethodCallStatement.append(")");

      boolean isMethodThrowingExceptions = !methodInjectionTarget.exceptionTypes.isEmpty();
      if (isMethodThrowingExceptions) {
        injectMethodBuilder.beginControlFlow("try");
      }
      injectMethodBuilder.addStatement(injectedMethodCallStatement.toString());
      if (isMethodThrowingExceptions) {
        int exceptionCounter = 1;
        for (TypeElement exceptionType : methodInjectionTarget.exceptionTypes) {
          injectMethodBuilder.nextControlFlow("catch ($T e$L)", exceptionType, exceptionCounter);
          injectMethodBuilder.addStatement(
              "throw new $T(e$L)", RuntimeException.class, exceptionCounter);
          exceptionCounter++;
        }

        injectMethodBuilder.endControlFlow();
      }
    }
  }

  private void emitInjectFields(
      List<FieldInjectionTarget> fieldInjectionTargetList, MethodSpec.Builder injectBuilder) {
    if (fieldInjectionTargetList == null) {
      return;
    }
    for (FieldInjectionTarget memberInjectionTarget : fieldInjectionTargetList) {
      CodeBlock invokeScopeGetMethodWithNameCodeBlock =
          getInvokeScopeGetMethodWithNameCodeBlock(memberInjectionTarget);
      injectBuilder.addCode("target.$L = scope.", memberInjectionTarget.memberName);
      injectBuilder.addCode(invokeScopeGetMethodWithNameCodeBlock);
      injectBuilder.addCode(";");
      injectBuilder.addCode(LINE_SEPARATOR);
    }
  }

  @Override
  public String getFqcn() {
    return getGeneratedFQNClassName(targetClass) + MEMBER_INJECTOR_SUFFIX;
  }
}
