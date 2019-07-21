package toothpick.kotlin

import com.example.toothpick.ktp.DelegateProvider

inline fun <reified T: Any> inject(name: String? = null): DelegateProvider<T> {
    return DelegateProvider(T::class.java, name)
}