package toothpick.kotlin

import toothpick.config.Binding
import toothpick.config.Module
import javax.inject.Provider
import kotlin.reflect.KClass

fun module(block: ModuleBuilder.() -> Unit): Module = ModuleBuilder().apply(block).module

class ModuleBuilder {
    val module = Module()

    inline infix fun <reified T : Any> KClass<T>.named(name: KClass<out Annotation>): Binding<T> {
        return module.bind(T::class.java) named name
    }

    inline infix fun <reified T : Any> KClass<T>.named(name: String): Binding<T> {
        return module.bind(T::class.java) named name
    }

    inline infix fun <reified T : Any> KClass<T>.providedAs(clazz: KClass<out T>): Binding<T>.BoundStateForClassBinding {
        return module.bind(T::class.java) providedAs clazz
    }

    inline infix fun <reified T : Any> KClass<T>.providedBy(provider: KClass<out Provider<T>>): Binding<T>.BoundStateForProviderClassBinding {
        return module.bind(T::class.java) providedBy provider
    }

    inline infix fun <reified T : Any> KClass<T>.providedBy(provider: Provider<T>): Binding<T>.BoundStateForProviderInstanceBinding {
        return module.bind(T::class.java) providedBy provider
    }

    inline infix fun <reified T : Any> KClass<T>.singletonProvidedBy(provider: KClass<out Provider<T>>) {
        module.bind(T::class.java) singletonProvidedBy provider
    }

    inline infix fun <reified T : Any> KClass<T>.singletonProvidedBy(provider: Provider<T>) {
        module.bind(T::class.java) singletonProvidedBy provider
    }

    inline infix fun <reified T : Any> KClass<T>.providedBy(instance: T) {
        module.bind(T::class.java) providedBy instance
    }
}

infix fun <T : Any> Binding<T>.named(name: KClass<out Annotation>): Binding<T> {
    return withName(name.java)
}

infix fun <T : Any> Binding<T>.named(name: String): Binding<T> {
    return withName(name)
}

infix fun <T : Any> Binding<T>.providedBy(instance: T) {
    toInstance(instance)
}

infix fun <T : Any> Binding<T>.providedAs(clazz: KClass<out T>): Binding<T>.BoundStateForClassBinding {
    return to(clazz.java)
}

infix fun <T : Any> Binding<T>.providedBy(provider: KClass<out Provider<T>>): Binding<T>.BoundStateForProviderClassBinding {
    return toProvider(provider.java)
}

infix fun <T : Any> Binding<T>.providedBy(provider: Provider<T>): Binding<T>.BoundStateForProviderInstanceBinding {
    return toProviderInstance(provider)
}

infix fun <T : Any> Binding<T>.singletonProvidedBy(provider: KClass<out Provider<T>>) {
    providedBy(provider).providesSingletonInScope()
}

infix fun <T : Any> Binding<T>.singletonProvidedBy(provider: Provider<T>) {
    providedBy(provider).providesSingletonInScope()
}
