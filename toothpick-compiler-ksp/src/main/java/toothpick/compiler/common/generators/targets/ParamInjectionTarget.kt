/*
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
package toothpick.compiler.common.generators.targets

import javax.lang.model.element.TypeElement

open class ParamInjectionTarget(
    val memberClass: TypeElement,
    val memberName: String,
    val kind: Kind?,
    val kindParamClass: TypeElement,
    val name: Any?
) {
    enum class Kind {
        INSTANCE, PROVIDER, LAZY
    }
}