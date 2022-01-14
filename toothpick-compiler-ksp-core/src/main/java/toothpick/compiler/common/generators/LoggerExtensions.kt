package toothpick.compiler.common.generators

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode

fun KSPLogger.error(message: String, vararg args: Any?) =
    error(message.format(*args))

fun KSPLogger.error(element: KSNode?, message: String, vararg args: Any?) =
    error(message.format(*args), element)

fun KSPLogger.warn(element: KSNode?, message: String, vararg args: Any?) =
    warn(message.format(*args), element)

fun KSPLogger.info(message: String, vararg args: Any?) =
    info(message.format(*args))
