/*
 * Copyright 2022 Baptiste Candellier
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.compiler.common.generators

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode

fun KSPLogger.error(node: String, vararg args: Any?) =
    error(node.format(*args))

fun KSPLogger.error(node: KSNode?, message: String, vararg args: Any?) =
    error(message.format(*args), node)

fun KSPLogger.warn(node: KSNode?, message: String, vararg args: Any?) =
    warn(message.format(*args), node)

fun KSPLogger.info(message: String, vararg args: Any?) =
    info(message.format(*args))
