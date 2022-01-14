package toothpick.compiler.common.generators

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import toothpick.compiler.common.generators.targets.ParamInjectionTarget
import javax.lang.model.util.Types

internal fun ParamInjectionTarget.getInvokeScopeGetMethodWithNameCodeBlock(): CodeBlock {
    val injectionName = if (name == null) ""
    else ", \"$name\""

    checkNotNull(kind) { "The kind can't be null." }

    val scopeGetMethodName: String = when (kind) {
        ParamInjectionTarget.Kind.INSTANCE -> "getInstance"
        ParamInjectionTarget.Kind.PROVIDER -> "getProvider"
        ParamInjectionTarget.Kind.LAZY -> "getLazy"
    }

    val className: ClassName = when (kind) {
        ParamInjectionTarget.Kind.INSTANCE -> memberClass.asClassName()
        ParamInjectionTarget.Kind.PROVIDER -> kindParamClass.asClassName()
        ParamInjectionTarget.Kind.LAZY -> kindParamClass.asClassName()
    }

    return CodeBlock.builder()
        .add("%N(%T::class.java%L)", scopeGetMethodName, className, injectionName)
        .build()
}

internal fun ParamInjectionTarget.getParamType(typeUtil: Types): TypeName {
    return when (kind) {
        ParamInjectionTarget.Kind.INSTANCE ->
            memberClass.asType().erased(typeUtil).asTypeName()
        else -> {
            memberClass.asClassName()
                .parameterizedBy(
                    kindParamClass.asType().erased(typeUtil).asTypeName()
                )
        }
    }
}
