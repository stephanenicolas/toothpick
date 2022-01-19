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
package toothpick.compiler

import org.junit.Test
import toothpick.compiler.factory.FactoryProcessorProvider
import toothpick.compiler.memberinjector.MemberInjectorProcessorProvider

class RelaxedFactoryForScopeInstancesTest {

    @Test
    fun testOptimisticFactoryCreationForHasScopeInstances_shouldFail_whenThereIsNoScopeAnnotation() {
        val source = javaSource(
            "TestOptimisticFactoryCreationForHasScopeInstances",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.ProvidesSingleton;
            @ProvidesSingleton
            public class TestOptimisticFactoryCreationForHasScopeInstances {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "The type test.TestOptimisticFactoryCreationForHasScopeInstances" +
                    " uses @ProvidesSingleton but doesn't have a scope annotation."
            )
    }

    @Test
    fun testOptimisticFactoryCreationForHasScopeInstances_shouldWork_whenThereIsAScopeAnnotation() {
        val source = javaSource(
            "TestOptimisticFactoryCreationForHasScopeInstances",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Scope;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import toothpick.ProvidesSingleton;
            @Scope
            @Retention(RetentionPolicy.RUNTIME)
            @interface CustomScope {}
            @ProvidesSingleton @CustomScope
            public class TestOptimisticFactoryCreationForHasScopeInstances {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesFileNamed(
                "test/TestOptimisticFactoryCreationForHasScopeInstances__Factory.kt"
            )
    }

    @Test
    fun testOptimisticFactoryCreationForHasScopeInstances_shouldFail_whenThereIsAScopeAnnotationWithWrongRetention() {
        val source = javaSource(
            "TestOptimisticFactoryCreationForHasScopeInstances",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Scope;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import toothpick.ProvidesSingleton;
            @Scope
            @Retention(RetentionPolicy.CLASS)
            @interface CustomScope {}
            @ProvidesSingleton @CustomScope
            public class TestOptimisticFactoryCreationForHasScopeInstances {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
    }
}
