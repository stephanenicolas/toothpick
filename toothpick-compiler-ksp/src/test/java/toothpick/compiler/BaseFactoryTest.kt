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
package toothpick.compiler

import com.tschuchort.compiletesting.SourceFile
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import toothpick.compiler.factory.FactoryProcessorProvider
import toothpick.compiler.memberinjector.MemberInjectorProcessorProvider

fun assertThatCompileWithoutErrorButNoFactoryIsCreated(
    source: SourceFile,
    noFactoryClass: String
) {
    try {
        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesFileNamed("test/${noFactoryClass}__Factory.kt")

        fail("A factory was created when it shouldn't.")
    } catch (e: Error) {
        assertTrue(e is AssertionError)
    }
}
