package com.example.toothpick.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toothpick.model.Backpack
import toothpick.ktp.delegate.inject

class BackpackViewModel : ViewModel() {
    val backpack: Backpack by inject()
}
