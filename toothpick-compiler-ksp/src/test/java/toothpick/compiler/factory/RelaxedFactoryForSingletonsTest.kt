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
package toothpick.compiler.factory

import org.junit.Test
import toothpick.compiler.*
import toothpick.compiler.factory.ProcessorTestUtilities.factoryAndMemberInjectorProcessors
import toothpick.compiler.factory.ProcessorTestUtilities.factoryProcessorsWithAdditionalTypes

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
            .processedWith(factoryAndMemberInjectorProcessors())
            .compilesWithoutError()
            .generatesFileNamed(
                "TestOptimisticFactoryCreationForSingleton__Factory.class"
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
            .processedWith(factoryProcessorsWithAdditionalTypes("test.CustomScope"))
            .compilesWithoutError()
            .generatesFileNamed(
                "TestOptimisticFactoryCreationForScopeAnnotation__Factory.class"
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
            .processedWith(factoryProcessorsWithAdditionalTypes("test.CustomScope"))
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
            .processedWith(factoryProcessorsWithAdditionalTypes("test.CustomScope"))
            .failsToCompile()
    }
}