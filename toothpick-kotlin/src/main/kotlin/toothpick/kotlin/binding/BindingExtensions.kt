package toothpick.kotlin.binding

import toothpick.config.Binding
import toothpick.config.Module
import javax.inject.Provider
import kotlin.reflect.KClass

// module
fun module(bindings: Module.() -> Unit) : Module = Module().apply { bindings() }

// bind
fun <T : Any> Module.bind(key: KClass<T>): Binding<T>.CanBeNamed = bind(key.java)
inline fun <reified T> Module.bind(): Binding<T>.CanBeNamed = bind(T::class.java)

// withName
fun <T : Any> Binding<T>.CanBeNamed.withName(annotationClassWithQualifierAnnotation: KClass<out Annotation>): Binding<T>.CanBeBound = withName(annotationClassWithQualifierAnnotation.java)
// https://youtrack.jetbrains.com/issue/KT-17061
// inline fun <T, reified A : Annotation> Binding<T>.CanBeNamed.withName() = withName(A::class.java)

// toClass
fun <T : Any> Binding<T>.CanBeBound.toClass(implClass: KClass<out T>): Binding<T>.CanBeSingleton = to(implClass.java)
inline fun <reified T> Binding<in T>.CanBeBound.toClass(): Binding<in T>.CanBeSingleton = to(T::class.java)

// toInstance
fun <T> Binding<T>.CanBeBound.toInstance(instanceProvider: () -> T) = toInstance(instanceProvider())

// toProvider
fun <T : Any> Binding<T>.CanBeBound.toProvider(providerClass: KClass<out Provider<out T>>): Binding<T>.CanProvideSingletonOrSingleton = toProvider(providerClass.java)
// https://youtrack.jetbrains.com/issue/KT-17061
// inline fun <T, reified P : Provider<out T>> Binding<T>.CanBeBound.toProvider(): Binding<T>.CanProvideSingletonOrSingleton = toProvider(P::class.java)
