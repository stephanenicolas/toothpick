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
package toothpick.compiler.factory.generators

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import toothpick.Factory
import toothpick.MemberInjector
import toothpick.Scope
import toothpick.compiler.common.generators.TPCodeGenerator
import toothpick.compiler.common.generators.factoryClassName
import toothpick.compiler.common.generators.getInvokeScopeGetMethodWithNameCodeBlock
import toothpick.compiler.common.generators.getParamType
import toothpick.compiler.common.generators.memberInjectorClassName
import toothpick.compiler.factory.targets.ConstructorInjectionTarget
import javax.inject.Singleton

/**
 * Generates a [Factory] for a given [ConstructorInjectionTarget]. Typically a factory
 * is created for a class a soon as it contains an [javax.inject.Inject] annotated
 * constructor. See Optimistic creation of factories in TP wiki.
 */
@OptIn(KotlinPoetKspPreview::class)
internal class FactoryGenerator(
    private val constructorInjectionTarget: ConstructorInjectionTarget
) : TPCodeGenerator {

    private val sourceClass: KSClassDeclaration = constructorInjectionTarget.builtClass

    val sourceClassName: ClassName = sourceClass.toClassName()
    val generatedClassName: ClassName = sourceClassName.factoryClassName

    override fun brewCode(): FileSpec {
        return FileSpec.get(
            packageName = sourceClassName.packageName,
            TypeSpec.classBuilder(generatedClassName)
                .addOriginatingKSFile(sourceClass.containingFile!!)
                .addModifiers(KModifier.INTERNAL)
                .addSuperinterface(
                    Factory::class.asClassName().parameterizedBy(sourceClassName)
                )
                .addAnnotation(
                    AnnotationSpec.builder(Suppress::class)
                        .addMember("%S", "ClassName")
                        .build()
                )
                .emitSuperMemberInjectorFieldIfNeeded()
                .emitCreateInstance()
                .emitGetTargetScope()
                .emitHasScopeAnnotation()
                .emitHasSingletonAnnotation()
                .emitHasReleasableAnnotation()
                .emitHasProvidesSingletonAnnotation()
                .emitHasProvidesReleasableAnnotation()
                .build()
        )
    }

    private fun TypeSpec.Builder.emitSuperMemberInjectorFieldIfNeeded() = apply {
        val superTypeThatNeedsInjection: ClassName =
            constructorInjectionTarget
                .superClassThatNeedsMemberInjection
                ?.toClassName()
                ?: return this

        val memberInjectorSuper: ParameterizedTypeName =
            MemberInjector::class.asClassName()
                .parameterizedBy(superTypeThatNeedsInjection)

        addProperty(
            PropertySpec
                .builder("memberInjector", memberInjectorSuper, KModifier.PRIVATE)
                .initializer("%T()", superTypeThatNeedsInjection.memberInjectorClassName)
                .build()
        )
    }

    private fun TypeSpec.Builder.emitCreateInstance(): TypeSpec.Builder = apply {
        val createInstanceBuilder =
            FunSpec.builder("createInstance")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("scope", Scope::class)
                .returns(sourceClassName)
                .apply {
                    // change the scope to target scope so that all dependencies are created in the target scope
                    // and the potential injection take place in the target scope too
                    if (constructorInjectionTarget.parameters.isNotEmpty() ||
                        constructorInjectionTarget.superClassThatNeedsMemberInjection != null
                    ) {
                        // We only need it when the constructor contains parameters or dependencies
                        addStatement("val scope = getTargetScope(scope)")
                    }
                }

        val varName = sourceClassName.simpleName
            .replaceFirstChar { first -> first.lowercaseChar() }

        val codeBlockBuilder = CodeBlock.builder()
            .apply {
                constructorInjectionTarget.parameters.forEachIndexed { i, param ->
                    addStatement(
                        "val %N: %T = scope.%L",
                        "param${i + 1}",
                        param.getParamType(),
                        param.getInvokeScopeGetMethodWithNameCodeBlock()
                    )
                }

                addStatement(
                    "val %N: %T = %T(%L)",
                    varName,
                    sourceClassName,
                    sourceClassName,
                    List(constructorInjectionTarget.parameters.size) { i -> "param${i + 1}" }
                        .joinToString(", ")
                )

                if (constructorInjectionTarget.superClassThatNeedsMemberInjection != null) {
                    addStatement("memberInjector.inject(%N, scope)", varName)
                }

                addStatement("return %N", varName)
            }

        createInstanceBuilder.addCode(codeBlockBuilder.build())

        addFunction(createInstanceBuilder.build())
    }

    private fun TypeSpec.Builder.emitGetTargetScope(): TypeSpec.Builder = apply {
        addFunction(
            FunSpec.builder("getTargetScope")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("scope", Scope::class)
                .returns(Scope::class)
                .addStatement(
                    "return scope%L",
                    parentScopeCodeBlock.toString()
                )
                .build()
        )
    }

    private fun TypeSpec.Builder.emitHasScopeAnnotation(): TypeSpec.Builder = apply {
        val scopeName = constructorInjectionTarget.scopeName
        val hasScopeAnnotation = scopeName != null
        addFunction(
            FunSpec.builder("hasScopeAnnotation")
                .addModifiers(KModifier.OVERRIDE)
                .returns(Boolean::class)
                .addStatement("return %L", hasScopeAnnotation)
                .build()
        )
    }

    private fun TypeSpec.Builder.emitHasSingletonAnnotation(): TypeSpec.Builder = apply {
        addFunction(
            FunSpec.builder("hasSingletonAnnotation")
                .addModifiers(KModifier.OVERRIDE)
                .returns(Boolean::class)
                .addStatement(
                    "return %L",
                    constructorInjectionTarget.hasSingletonAnnotation
                )
                .build()
        )
    }

    private fun TypeSpec.Builder.emitHasReleasableAnnotation(): TypeSpec.Builder {
        addFunction(
            FunSpec.builder("hasReleasableAnnotation")
                .addModifiers(KModifier.OVERRIDE)
                .returns(Boolean::class)
                .addStatement(
                    "return %L",
                    constructorInjectionTarget.hasReleasableAnnotation
                )
                .build()
        )
        return this
    }

    private fun TypeSpec.Builder.emitHasProvidesSingletonAnnotation(): TypeSpec.Builder = apply {
        addFunction(
            FunSpec.builder("hasProvidesSingletonAnnotation")
                .addModifiers(KModifier.OVERRIDE)
                .returns(Boolean::class)
                .addStatement(
                    "return %L",
                    constructorInjectionTarget.hasProvidesSingletonAnnotation
                )
                .build()
        )
    }

    private fun TypeSpec.Builder.emitHasProvidesReleasableAnnotation(): TypeSpec.Builder = apply {
        addFunction(
            FunSpec.builder("hasProvidesReleasableAnnotation")
                .addModifiers(KModifier.OVERRIDE)
                .returns(Boolean::class)
                .addStatement(
                    "return %L",
                    constructorInjectionTarget.hasProvidesReleasableAnnotation
                )
                .build()
        )
    }

    private val parentScopeCodeBlock: CodeBlock
        get() = when (val scopeName = constructorInjectionTarget.scopeName) {
            null -> CodeBlock.of("")
            // there is no scope name or the current @Scoped annotation.
            Singleton::class.java.name -> CodeBlock.of(".rootScope")
            else -> CodeBlock.of(".getParentScope(%L::class.java)", scopeName)
        }
}
