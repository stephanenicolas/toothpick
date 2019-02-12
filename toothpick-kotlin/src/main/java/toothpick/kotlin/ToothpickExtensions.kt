package toothpick.kotlin

import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Binding
import toothpick.config.Module
import kotlin.reflect.KClass

fun <T : Any> Module.bind(clazz: KClass<T>): Binding<T> {
    return bind(clazz.java)
}

fun Scope.inject(toInject: Any) {
    Toothpick.inject(toInject, this)
}

fun <T : Any> Scope.getInstance(clazz: KClass<T>, name: String? = null, annotationName: KClass<out Annotation>? = null): T {
    return if (name.isNullOrBlank() && annotationName == null) {
        getInstance(clazz.java)
    } else {
        getInstance(clazz.java, name ?: annotationName?.java?.name)
    }
}

inline fun <reified T: Any> Scope.getInstance(name: String? = null, annotationName: KClass<out Annotation>? = null): T {
    return getInstance(T::class, name, annotationName)
}

fun <T : Any> inject(clazz: KClass<T>, scope: Scope, name: String? = null, annotationName: KClass<out Annotation>? = null): T {
    return scope.getInstance(clazz, name, annotationName)
}

inline fun <reified T : Any> inject(scope: Scope, name: String? = null, annotationName: KClass<out Annotation>? = null): T {
    return scope.getInstance(T::class, name, annotationName)
}

inline fun <reified T : Any> injectLazy(
    scope: Scope? = null,
    name: String? = null,
    annotationName: KClass<out Annotation>? = null,
    noinline injectConfig: InputConfigBlock? = null
): ToothpickInjectDelegate<Any, T> {
    return ToothpickInjectDelegate(T::class, scope, name, annotationName, injectConfig)
}

interface HasScope {
    val scope: Scope
}

inline fun <reified T : Any> HasScope.inject(name: String? = null, annotationName: KClass<out Annotation>? = null): T {
    return scope.getInstance(T::class, name, annotationName)
}
