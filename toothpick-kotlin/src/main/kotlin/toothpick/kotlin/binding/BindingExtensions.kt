package toothpick.kotlin.binding

import toothpick.config.Binding
import toothpick.config.Module
import kotlin.reflect.KClass

// module
fun module(bindings: Module.() -> Unit) : Module = Module().apply { bindings() }

// bind
fun <T : Any> Module.bind(key: KClass<T>): Binding<T>.CanBeNamed = bind(key.java)
inline fun <reified T> Module.bind(): Binding<T>.CanBeNamed = bind(T::class.java)

// toClass
fun <T : Any> Binding<T>.CanBeBound.toClass(implClass: KClass<out T>): Binding<T>.CanBeSingleton = to(implClass.java)
inline fun <reified T> Binding<in T>.CanBeBound.toClass(): Binding<in T>.CanBeSingleton = to(T::class.java)

// toInstance
fun <T> Binding<T>.CanBeBound.toInstance(instanceProvider: () -> T) = toInstance(instanceProvider())
