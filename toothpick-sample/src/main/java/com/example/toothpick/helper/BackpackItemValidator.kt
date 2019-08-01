package com.example.toothpick.helper

import toothpick.Releasable
import javax.inject.Singleton

@Singleton
@Releasable
class BackpackItemValidator {
    fun isValidName(name: String) = name.isNotEmpty()
}