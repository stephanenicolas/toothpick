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
@file:OptIn(KspExperimental::class)

package toothpick.compiler.common.generators.targets

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import toothpick.compiler.common.generators.error
import javax.inject.Named
import javax.inject.Qualifier

/**
 * Information necessary to identify the parameter of a method or a class's property.
 */
sealed class VariableInjectionTarget(
    val memberType: KSType,
    val memberName: KSName,
    val qualifierName: Any?
) {
    class Instance(
        memberType: KSType,
        memberName: KSName,
        qualifierName: Any?
    ) : VariableInjectionTarget(memberType, memberName, qualifierName)

    class Lazy(
        memberType: KSType,
        memberName: KSName,
        qualifierName: Any?,
        val kindParamClass: KSType
    ) : VariableInjectionTarget(memberType, memberName, qualifierName)

    class Provider(
        memberType: KSType,
        memberName: KSName,
        qualifierName: Any?,
        val kindParamClass: KSType
    ) : VariableInjectionTarget(memberType, memberName, qualifierName)

    companion object {

        fun create(parameter: KSValueParameter, logger: KSPLogger? = null): VariableInjectionTarget =
            create(
                name = parameter.name!!,
                type = parameter.type.resolve(),
                qualifierName = parameter.findQualifierName(logger)
            )

        fun create(parameter: KSPropertyDeclaration, logger: KSPLogger? = null): VariableInjectionTarget =
            create(
                name = parameter.simpleName,
                type = parameter.type.resolve(),
                qualifierName = parameter.findQualifierName(logger)
            )

        private fun create(name: KSName, type: KSType, qualifierName: String?): VariableInjectionTarget =
            when (type.declaration.qualifiedName?.asString()) {
                javax.inject.Provider::class.qualifiedName ->
                    Provider(
                        memberType = type,
                        memberName = name,
                        qualifierName = qualifierName,
                        kindParamClass = type.getInjectedType()
                    )
                toothpick.Lazy::class.qualifiedName ->
                    Lazy(
                        memberType = type,
                        memberName = name,
                        qualifierName = qualifierName,
                        kindParamClass = type.getInjectedType()
                    )
                else -> Instance(
                    memberType = type,
                    memberName = name,
                    qualifierName = qualifierName
                )
            }

        /**
         * Lookup both [javax.inject.Qualifier] and [javax.inject.Named] to provide the name
         * of an injection.
         *
         * @receiver the node for which a qualifier is to be found.
         * @return the name of this injection, or null if it has no qualifier annotations.
         */
        private fun KSAnnotated.findQualifierName(logger: KSPLogger?): String? {
            val qualifierAnnotationNames = annotations
                .mapNotNull { annotation ->
                    val annotationClass = annotation.annotationType.resolve().declaration
                    val annotationClassName = annotationClass.qualifiedName?.asString()
                    if (annotationClass.isAnnotationPresent(Qualifier::class)) {
                        annotationClassName.takeIf { className ->
                            className != Named::class.qualifiedName
                        }
                    } else null
                }

            val namedValues = getAnnotationsByType(Named::class)
                .map { annotation -> annotation.value }

            val allNames = qualifierAnnotationNames + namedValues

            if (allNames.count() > 1) {
                logger?.error(this, "Only one javax.inject.Qualifier annotation is allowed to name injections.")
            }

            return allNames.firstOrNull()
        }

        /**
         * Retrieves the type of a field or param.
         *
         * @receiver The type to inspect.
         * @return Can be the type of a simple instance (e.g. in `b: B`, type is `B`).
         * But if the type is [toothpick.Lazy] or [javax.inject.Provider], then we use the type parameter
         * (e.g. in `Lazy<B>`, type is `B`, not `Lazy`).
         */
        private fun KSType.getInjectedType(): KSType = arguments.first().type!!.resolve()
    }
}

fun VariableInjectionTarget.getInvokeScopeGetMethodWithNameCodeBlock(): CodeBlock {
    val scopeGetMethodName: String = when (this) {
        is VariableInjectionTarget.Instance -> "getInstance"
        is VariableInjectionTarget.Provider -> "getProvider"
        is VariableInjectionTarget.Lazy -> "getLazy"
    }

    val className: ClassName = when (this) {
        is VariableInjectionTarget.Instance -> memberType.toClassName()
        is VariableInjectionTarget.Provider -> kindParamClass.toClassName()
        is VariableInjectionTarget.Lazy -> kindParamClass.toClassName()
    }

    return CodeBlock.builder()
        .add("%N(%T::class.java", scopeGetMethodName, className)
        .apply { if (qualifierName != null) add(", %S", qualifierName) }
        .add(")")
        .build()
}

fun VariableInjectionTarget.getParamType(): TypeName = when (this) {
    is VariableInjectionTarget.Instance -> memberType.toTypeName()
    is VariableInjectionTarget.Provider -> memberType.toClassName()
        .parameterizedBy(kindParamClass.toTypeName())
    is VariableInjectionTarget.Lazy -> memberType.toClassName()
        .parameterizedBy(kindParamClass.toTypeName())
}
