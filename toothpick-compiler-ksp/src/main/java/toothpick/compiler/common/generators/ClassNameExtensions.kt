package toothpick.compiler.common.generators

import com.squareup.kotlinpoet.ClassName

val ClassName.factoryClassName: ClassName
    get() = ClassName(
        packageName = packageName,
        simpleNames.joinToString("$") + "__Factory"
    )

val ClassName.memberInjectorClassName: ClassName
    get() = ClassName(
        packageName = packageName,
        simpleNames.joinToString("$") + "__MemberInjector"
    )
