package com.example.toothpick.kotlin

import javax.inject.Inject
import javax.inject.Singleton

@BackpackFlow.Scope @Singleton
data class Backpack @Inject constructor(val gears: Any)