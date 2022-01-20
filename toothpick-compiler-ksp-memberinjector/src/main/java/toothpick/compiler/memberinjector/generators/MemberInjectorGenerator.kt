/*
 * Copyright 2022 Baptiste Candellier
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

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import toothpick.MemberInjector
import toothpick.Scope
import toothpick.compiler.common.generators.TPCodeGenerator
import toothpick.compiler.common.generators.getInvokeScopeGetMethodWithNameCodeBlock
import toothpick.compiler.common.generators.getParamType
import toothpick.compiler.common.generators.memberInjectorClassName
import toothpick.compiler.common.generators.targets.FieldInjectionTarget
import toothpick.compiler.memberinjector.targets.MethodInjectionTarget

/**
 * Generates a [MemberInjector] for a given collection of [FieldInjectionTarget].
 * Typically a [MemberInjector] is created for a class a soon as it contains an [ ] annotated field or method.
 */
@OptIn(KotlinPoetKspPreview::class)
internal class MemberInjectorGenerator(
    private val sourceClass: KSClassDeclaration,
    private val superClassThatNeedsInjection: KSClassDeclaration?,
    private val fieldInjectionTargetList: List<FieldInjectionTarget>?,
    private val methodInjectionTargetList: List<MethodInjectionTarget>?
) : TPCodeGenerator {

    init {
        require(fieldInjectionTargetList != null || methodInjectionTargetList != null) {
            "At least one memberInjectorInjectionTarget is needed."
        }
    }

    val sourceClassName: ClassName = sourceClass.toClassName()
    val generatedClassName: ClassName = sourceClassName.memberInjectorClassName

    override fun brewCode(): FileSpec {
        return FileSpec.get(
            packageName = sourceClassName.packageName,
            TypeSpec.classBuilder(generatedClassName)
                .addOriginatingKSFile(sourceClass.containingFile!!)
                .addModifiers(sourceClass.getVisibility().toKModifier() ?: KModifier.PUBLIC)
                .addSuperinterface(
                    MemberInjector::class.asClassName().parameterizedBy(sourceClassName)
                )
                .addAnnotation(
                    AnnotationSpec.builder(Suppress::class)
                        .addMember("%S", "ClassName")
                        .addMember("%S", "RedundantVisibilityModifier")
                        .build()
                )
                .emitSuperMemberInjectorFieldIfNeeded()
                .emitInjectMethod(fieldInjectionTargetList, methodInjectionTargetList)
                .build()
        )
    }

    private fun TypeSpec.Builder.emitSuperMemberInjectorFieldIfNeeded(): TypeSpec.Builder = apply {
        if (superClassThatNeedsInjection == null) {
            return this
        }

        addProperty(
            // TODO use proper typing here
            PropertySpec.builder(
                "superMemberInjector",
                MemberInjector::class.asClassName()
                    .parameterizedBy(
                        superClassThatNeedsInjection.asStarProjectedType().toTypeName()
                    ),
                KModifier.PRIVATE
            )
                .initializer("%T()", superClassThatNeedsInjection.toClassName().memberInjectorClassName)
                .build()
        )
    }

    private fun TypeSpec.Builder.emitInjectMethod(
        fieldInjectionTargetList: List<FieldInjectionTarget>?,
        methodInjectionTargetList: List<MethodInjectionTarget>?
    ): TypeSpec.Builder = apply {
        addFunction(
            FunSpec.builder("inject")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("target", sourceClassName)
                .addParameter("scope", Scope::class)
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

    private fun FunSpec.Builder.emitInjectMethods(
        methodInjectionTargetList: List<MethodInjectionTarget>?
    ): FunSpec.Builder = apply {
        methodInjectionTargetList
            ?.filterNot { it.isOverride }
            ?.forEach { methodInjectionTarget ->
                methodInjectionTarget.parameters
                    .forEachIndexed { paramIndex, paramInjectionTarget ->
                        addStatement(
                            "val %N = scope.%L as %T",
                            "param${paramIndex + 1}",
                            paramInjectionTarget.getInvokeScopeGetMethodWithNameCodeBlock(),
                            paramInjectionTarget.getParamType()
                        )
                    }

                addStatement(
                    "target.%N(%L)",
                    methodInjectionTarget.methodName.asString(),
                    List(methodInjectionTarget.parameters.size) { paramIndex -> "param${paramIndex + 1}" }
                        .joinToString(", ")
                )
            }
    }

    private fun FunSpec.Builder.emitInjectFields(
        fieldInjectionTargetList: List<FieldInjectionTarget>?
    ): FunSpec.Builder = apply {
        fieldInjectionTargetList?.forEach { memberInjectionTarget ->
            addStatement(
                "target.%N = scope.%L as %T",
                memberInjectionTarget.memberName.asString(),
                memberInjectionTarget.getInvokeScopeGetMethodWithNameCodeBlock(),
                memberInjectionTarget.getParamType()
            )
        }
    }
}
