package com.example.toothpick.ktp

import toothpick.Toothpick

class KTP {
    companion object {
        val delegateNotifier = DelegateNotifier()

        fun openScope(name: Any) = KTPScope(Toothpick.openScope(name))

        fun openScopes(vararg names: Any) = KTPScope(Toothpick.openScopes(names))

        fun closeScope(name: Any) {
            Toothpick.closeScope(name)
        }
    }
}