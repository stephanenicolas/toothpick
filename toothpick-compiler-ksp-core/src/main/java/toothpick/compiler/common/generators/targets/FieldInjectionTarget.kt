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
package toothpick.compiler.common.generators.targets

import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType

class FieldInjectionTarget(
    memberType: KSType,
    memberName: KSName,
    kind: Kind?,
    kindParamClass: KSType,
    qualifierName: Any?
) : ParamInjectionTarget(memberType, memberName, kind, kindParamClass, qualifierName)
