package com.example.toothpick.ktp

import kotlin.reflect.KProperty

class DelegateProvider<T: Any>(private val clz: Class<T>, private val name: String?) {

    operator fun provideDelegate(thisRef: Any, prop: KProperty<*>): InjectDelegate<T> {
        val delegate = InjectDelegate(clz, name)
        KTP.delegateNotifier.registerDelegate(thisRef, delegate)
        return delegate
    }
}