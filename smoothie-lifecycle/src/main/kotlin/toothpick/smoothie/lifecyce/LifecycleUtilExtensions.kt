package toothpick.smoothie.lifecycle

import androidx.fragment.app.FragmentActivity
import toothpick.Scope

fun Scope.closeOnDestroy(activity: FragmentActivity): Scope {
    LifecycleUtil.closeOnDestroy(activity, this)
    return this
}
