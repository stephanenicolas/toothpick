package com.example.toothpick.helper

import com.example.toothpick.annotation.ApplicationScope
import toothpick.Releasable
import javax.inject.Singleton

@ApplicationScope
@Singleton
@Releasable
class BackpackItemValidator {
    fun isValidName(name: String) = name.isNotEmpty()
}