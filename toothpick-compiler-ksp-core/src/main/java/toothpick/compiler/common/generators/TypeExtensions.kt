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

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.asClassName

inline fun <reified T : Annotation> KSAnnotated.hasAnnotation(
    matches: (KSAnnotation) -> Boolean = { true }
): Boolean {
    return annotations.any { annotation ->
        val className = T::class.asClassName()
        annotation.shortName.asString() == className.simpleName &&
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() == className.canonicalName &&
            matches(annotation)
    }
}
