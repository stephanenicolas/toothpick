package com.example.toothpick.kotlin

import toothpick.Toothpick
import toothpick.ktp.KTP
import toothpick.ktp.KTPScope
import kotlin.annotation.AnnotationRetention.RUNTIME

class BackpackFlow {

    companion object {
        fun openScope(parentScope: KTPScope): KTPScope {
            return parentScope.openSubScope(Scope::class.java)
        }

        fun closeScope() {
            KTP.closeScope(Scope::class.java)
        }
    }

    @javax.inject.Scope
    @Retention(RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Scope
}

fun KTPScope.openBackpackFlowSubScope(): KTPScope {
    return BackpackFlow.openScope(this)
}


