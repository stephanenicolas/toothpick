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
package toothpick.compiler.factory

import org.junit.Test
import toothpick.compiler.common.ToothpickOptions.Companion.AdditionalAnnotationTypes
import toothpick.compiler.compilationAssert
import toothpick.compiler.compilesWithoutError
import toothpick.compiler.failsToCompile
import toothpick.compiler.generatesFileNamed
import toothpick.compiler.javaSource
import toothpick.compiler.processedWith
import toothpick.compiler.that
import toothpick.compiler.withOptions

class RelaxedFactoryForSingletonsTest {

    @Test
    fun testOptimisticFactoryCreationForSingleton() {
        val source = javaSource(
            "TestOptimisticFactoryCreationForSingleton",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Singleton;
            @Singleton
            public class TestOptimisticFactoryCreationForSingleton {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider()) // TODO used to use MemberInjector as well; really necessary?
            .compilesWithoutError()
            .generatesFileNamed(
                "test/TestOptimisticFactoryCreationForSingleton__Factory.kt"
            )
    }

    @Test
    fun testOptimisticFactoryCreationForScopeAnnotation() {
        val source = javaSource(
            "TestOptimisticFactoryCreationForScopeAnnotation",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Scope;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            @Scope
            @Retention(RetentionPolicy.RUNTIME)
            @interface CustomScope {}
            @CustomScope
            public class TestOptimisticFactoryCreationForScopeAnnotation {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .withOptions(AdditionalAnnotationTypes to "test.CustomScope")
            .compilesWithoutError()
            .generatesFileNamed(
                "test/TestOptimisticFactoryCreationForScopeAnnotation__Factory.kt"
            )
    }

    @Test
    fun testOptimisticFactoryCreationForScopeAnnotation_shouldFail_WhenScopeAnnotationDoesNotHaveRetention() {
        val source = javaSource(
            "TestOptimisticFactoryCreationForScopeAnnotation",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Scope;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            @Scope
            @interface CustomScope {}
            @CustomScope
            public class TestOptimisticFactoryCreationForScopeAnnotation {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .withOptions(AdditionalAnnotationTypes to "test.CustomScope")
            .failsToCompile()
    }

    @Test
    fun testOptimisticFactoryCreationForScopeAnnotation_shouldFail_WhenScopeAnnotationDoesNotHaveRuntimeRetention() {
        val source = javaSource(
            "TestOptimisticFactoryCreationForScopeAnnotation",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Scope;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            @Scope
            @Retention(RetentionPolicy.CLASS)
            @interface CustomScope {}
            @CustomScope
            public class TestOptimisticFactoryCreationForScopeAnnotation {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .withOptions(AdditionalAnnotationTypes to "test.CustomScope")
            .failsToCompile()
    }
}
