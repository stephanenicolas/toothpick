package toothpick.kotlin.androidx

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import toothpick.Scope
import toothpick.Toothpick

object ContextScopeFinder {
    fun findScope(context: Context): Scope {
        return when (context::class.java) {
            android.view.ContextThemeWrapper::class.java -> findScope((context as android.view.ContextThemeWrapper).baseContext)
            ContextThemeWrapper::class.java -> findScope((context as ContextThemeWrapper).baseContext)
            else -> Toothpick.openScope(context)
        }
    }
}
