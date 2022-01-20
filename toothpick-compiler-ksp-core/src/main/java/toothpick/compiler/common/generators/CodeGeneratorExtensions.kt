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
@file:OptIn(KotlinPoetKspPreview::class)

package toothpick.compiler.common.generators

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import toothpick.compiler.common.generators.targets.ParamInjectionTarget

fun ParamInjectionTarget.getInvokeScopeGetMethodWithNameCodeBlock(): CodeBlock {
    checkNotNull(kind) { "The kind can't be null." }

    val scopeGetMethodName: String = when (kind) {
        ParamInjectionTarget.Kind.INSTANCE -> "getInstance"
        ParamInjectionTarget.Kind.PROVIDER -> "getProvider"
        ParamInjectionTarget.Kind.LAZY -> "getLazy"
    }

    val className: ClassName = when (kind) {
        ParamInjectionTarget.Kind.INSTANCE -> memberType.toClassName()
        ParamInjectionTarget.Kind.PROVIDER -> kindParamClass.toClassName()
        ParamInjectionTarget.Kind.LAZY -> kindParamClass.toClassName()
    }

    return CodeBlock.builder()
        .add("%N(%T::class.java", scopeGetMethodName, className)
        .apply { if (name != null) add(", %S", name) }
        .add(")")
        .build()
}

fun ParamInjectionTarget.getParamType(): TypeName {
    return when (kind) {
        ParamInjectionTarget.Kind.INSTANCE -> memberType.toTypeName()
        else -> memberType.toClassName().parameterizedBy(kindParamClass.toTypeName())
    }
}
