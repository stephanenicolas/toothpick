package com.example.toothpick.helper

import android.view.View
import com.google.android.material.snackbar.Snackbar
import toothpick.Releasable
import javax.inject.Singleton

@Singleton
@Releasable
class NotificationHelper {
    fun showNotification(attachTo: View, message: String) {
        Snackbar.make(
                attachTo,
                message,
                Snackbar.LENGTH_SHORT
        ).show()
    }
}
