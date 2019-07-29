package toothpick.ktp.extension

import toothpick.Lazy
import toothpick.Scope
import javax.inject.Provider

inline fun <reified T> Scope.getInstance(name: String? = null): T = this.getInstance(T::class.java, name)
inline fun <reified T> Scope.getLazy(name: String? = null): Lazy<T> = this.getLazy(T::class.java, name)
inline fun <reified T> Scope.getProvider(name: String? = null): Provider<T> = this.getProvider(T::class.java, name)
