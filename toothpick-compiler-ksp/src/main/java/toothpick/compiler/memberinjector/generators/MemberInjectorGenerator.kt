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
package toothpick.compiler.memberinjector.generators

import com.squareup.javapoet.*
import toothpick.MemberInjector
import toothpick.Scope
import toothpick.compiler.common.generators.CodeGenerator
import toothpick.compiler.memberinjector.targets.FieldInjectionTarget
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

/**
 * Generates a [MemberInjector] for a given collection of [FieldInjectionTarget].
 * Typically a [MemberInjector] is created for a class a soon as it contains an [ ] annotated field or method.
 */
class MemberInjectorGenerator(
    private val targetClass: TypeElement,
    private val superClassThatNeedsInjection: TypeElement?,
    private val fieldInjectionTargetList: List<FieldInjectionTarget>?,
    private val methodInjectionTargetList: List<MethodInjectionTarget>?,
    types: Types
) : CodeGenerator(types) {

    init {
        require(!(fieldInjectionTargetList == null && methodInjectionTargetList == null)) {
            "At least one memberInjectorInjectionTarget is needed."
        }
    }

    override fun brewJava(): String {
        // Interface to implement
        val className = ClassName.get(targetClass)
        val memberInjectorInterfaceParameterizedTypeName =
            ParameterizedTypeName.get(ClassName.get(MemberInjector::class.java), className)

        // Build class
        val scopeMemberTypeSpec =
            TypeSpec.classBuilder(getGeneratedSimpleClassName(targetClass) + MEMBER_INJECTOR_SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(memberInjectorInterfaceParameterizedTypeName)
                .emitSuperMemberInjectorFieldIfNeeded()
                .emitInjectMethod(fieldInjectionTargetList, methodInjectionTargetList)

        return JavaFile.builder(className.packageName(), scopeMemberTypeSpec.build())
            .build()
            .toString()
    }

    private fun TypeSpec.Builder.emitSuperMemberInjectorFieldIfNeeded(): TypeSpec.Builder = apply {
        if (superClassThatNeedsInjection != null) {
            addField(
                // TODO use proper typing here
                FieldSpec.builder(
                    ParameterizedTypeName.get(
                        ClassName.get(MemberInjector::class.java),
                        TypeName.get(typeUtil.erasure(superClassThatNeedsInjection.asType()))
                    ),
                    "superMemberInjector",
                    Modifier.PRIVATE
                )
                    .initializer(
                        "new \$L__MemberInjector()",
                        getGeneratedFQNClassName(superClassThatNeedsInjection)
                    )
                    .build()
            )
        }
    }

    private fun TypeSpec.Builder.emitInjectMethod(
        fieldInjectionTargetList: List<FieldInjectionTarget>?,
        methodInjectionTargetList: List<MethodInjectionTarget>?
    ): TypeSpec.Builder = apply {
        addMethod(
            MethodSpec.methodBuilder("inject")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(targetClass), "target")
                .addParameter(ClassName.get(Scope::class.java), "scope")
                .apply {
                    if (superClassThatNeedsInjection != null) {
                        addStatement("superMemberInjector.inject(target, scope)")
                    }
                }
                .emitInjectFields(fieldInjectionTargetList)
                .emitInjectMethods(methodInjectionTargetList)
                .build()
        )
    }

    private fun MethodSpec.Builder.emitInjectMethods(
        methodInjectionTargetList: List<MethodInjectionTarget>?
    ): MethodSpec.Builder = apply {
        if (methodInjectionTargetList != null) {
            var counter = 1
            for (methodInjectionTarget in methodInjectionTargetList) {
                if (methodInjectionTarget.isOverride) {
                    continue
                }
                val injectedMethodCallStatement = StringBuilder()
                injectedMethodCallStatement.append("target.")
                injectedMethodCallStatement.append(methodInjectionTarget.methodName)
                injectedMethodCallStatement.append("(")
                var prefix = ""
                for (paramInjectionTarget in methodInjectionTarget.parameters) {
                    val invokeScopeGetMethodWithNameCodeBlock =
                        getInvokeScopeGetMethodWithNameCodeBlock(paramInjectionTarget)
                    val paramName = "param" + counter++
                    addCode(
                        "\$T \$L = scope.", getParamType(paramInjectionTarget), paramName
                    )
                    addCode(invokeScopeGetMethodWithNameCodeBlock)
                    addCode(";")
                    addCode(LINE_SEPARATOR)
                    injectedMethodCallStatement.append(prefix)
                    injectedMethodCallStatement.append(paramName)
                    prefix = ", "
                }

                injectedMethodCallStatement.append(")")

                val isMethodThrowingExceptions = !methodInjectionTarget.exceptionTypes.isEmpty()
                if (isMethodThrowingExceptions) {
                    beginControlFlow("try")
                }

                addStatement(injectedMethodCallStatement.toString())

                if (isMethodThrowingExceptions) {
                    var exceptionCounter = 1
                    for (exceptionType in methodInjectionTarget.exceptionTypes) {
                        nextControlFlow("catch (\$T e\$L)", exceptionType, exceptionCounter)
                        addStatement(
                            "throw new \$T(e\$L)", RuntimeException::class.java, exceptionCounter
                        )
                        exceptionCounter++
                    }
                    endControlFlow()
                }
            }
        }
    }

    private fun MethodSpec.Builder.emitInjectFields(
        fieldInjectionTargetList: List<FieldInjectionTarget>?
    ): MethodSpec.Builder = apply {
        if (fieldInjectionTargetList != null) {
            for (memberInjectionTarget in fieldInjectionTargetList) {
                val invokeScopeGetMethodWithNameCodeBlock =
                    getInvokeScopeGetMethodWithNameCodeBlock(memberInjectionTarget)

                addCode("target.\$L = scope.", memberInjectionTarget.memberName)
                addCode(invokeScopeGetMethodWithNameCodeBlock)
                addCode(";")
                addCode(LINE_SEPARATOR)
            }
        }
    }

    override val fqcn: String
        get() = getGeneratedFQNClassName(targetClass) + MEMBER_INJECTOR_SUFFIX

    companion object {
        private const val MEMBER_INJECTOR_SUFFIX = "__MemberInjector"
    }
}