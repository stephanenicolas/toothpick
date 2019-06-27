package com.example.toothpick.ktp

import toothpick.Scope
import java.lang.IllegalStateException
import kotlin.reflect.KProperty

class InjectDelegate<T: Any>(val clz: Class<T>, val name: String?) {

    lateinit var instance: T

    fun scopeReady(scope: Scope) {
        instance = scope.getInstance(clz, name)
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (this::instance.isInitialized) {
            throw IllegalStateException("The dependency has not be injected yet.")
        }
        return instance
    }
}