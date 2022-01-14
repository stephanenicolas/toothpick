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
        ParamInjectionTarget.Kind.INSTANCE -> memberClass.toClassName()
        ParamInjectionTarget.Kind.PROVIDER -> kindParamClass.toClassName()
        ParamInjectionTarget.Kind.LAZY -> kindParamClass.toClassName()
    }

    return CodeBlock.builder()
        .add("%N(%T::class.java", scopeGetMethodName, className)
        .apply { if (name != null) add(", %S", name)  }
        .add(")")
        .build()
}

fun ParamInjectionTarget.getParamType(): TypeName {
    return when (kind) {
        ParamInjectionTarget.Kind.INSTANCE -> memberClass.asStarProjectedType().toTypeName()
        else -> memberClass.toClassName().parameterizedBy(kindParamClass.toTypeName())
    }
}
