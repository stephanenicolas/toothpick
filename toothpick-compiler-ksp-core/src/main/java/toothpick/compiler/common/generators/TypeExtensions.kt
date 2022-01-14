package toothpick.compiler.common.generators

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.asClassName
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

fun TypeMirror.asElement(typeUtils: Types): Element? = typeUtils.asElement(this)

inline fun <reified T : Annotation> KSAnnotated.hasAnnotation(
    matches: (KSAnnotation) -> Boolean = { true }
): Boolean {
    return annotations.any { annotation ->
        val className = T::class.asClassName()
        annotation.shortName.asString() == className.simpleName
            && annotation.annotationType.resolve().declaration.qualifiedName?.asString() == className.canonicalName
            && matches(annotation)
    }
}
