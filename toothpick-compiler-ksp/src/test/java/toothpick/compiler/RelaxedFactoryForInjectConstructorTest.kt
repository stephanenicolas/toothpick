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

class RelaxedFactoryForInjectConstructorTest {

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldWork_whenDefaultConstructorIsPresent() {
        val source = javaSource(
            "TestOptimisticFactoryCreationForInjectConstructor",
            """
            package test;
            import toothpick.InjectConstructor;
            @InjectConstructor
            public class TestOptimisticFactoryCreationForInjectConstructor {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesFileNamed(
                "test/TestOptimisticFactoryCreationForInjectConstructor__Factory.kt"
            )
    }

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldUse_uniqueConstructorWhenAnnotated() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import toothpick.InjectConstructor;
            import toothpick.Lazy;
            @InjectConstructor
            public class TestNonEmptyConstructor {
              public TestNonEmptyConstructor(Lazy<String> str, Integer n) {}
            }
            """
        )

        val expectedSource = expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Lazy
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1: Lazy<String> = scope.getLazy(String::class.java)
                val param2: Int = scope.getInstance(Int::class.java)
                val testNonEmptyConstructor: TestNonEmptyConstructor = TestNonEmptyConstructor(param1, param2)
                return testNonEmptyConstructor
              }
            
              public override fun getTargetScope(scope: Scope): Scope = scope
            
              public override fun hasScopeAnnotation(): Boolean = false
            
              public override fun hasSingletonAnnotation(): Boolean = false
            
              public override fun hasReleasableAnnotation(): Boolean = false
            
              public override fun hasProvidesSingletonAnnotation(): Boolean = false
            
              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldHave_referenceToMemberInjector() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import toothpick.InjectConstructor;
            import javax.inject.Inject;
            import toothpick.Lazy;
            @InjectConstructor
            public class TestNonEmptyConstructor {
              @Inject String string;
              public TestNonEmptyConstructor(Lazy<String> str, Integer n) {}
            }
            """
        )

        val expectedSource = expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Lazy
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1: Lazy<String> = scope.getLazy(String::class.java)
                val param2: Int = scope.getInstance(Int::class.java)
                val testNonEmptyConstructor: TestNonEmptyConstructor = TestNonEmptyConstructor(param1, param2)
                return testNonEmptyConstructor
              }
            
              public override fun getTargetScope(scope: Scope): Scope = scope
            
              public override fun hasScopeAnnotation(): Boolean = false
            
              public override fun hasSingletonAnnotation(): Boolean = false
            
              public override fun hasReleasableAnnotation(): Boolean = false
            
              public override fun hasProvidesSingletonAnnotation(): Boolean = false
            
              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldFail_uniqueConstructorIsAnnotated() {
        val source = javaSource(
            "TestNonEmptyConstructorInjected",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.InjectConstructor;
            import toothpick.Lazy;
            @InjectConstructor
            public class TestNonEmptyConstructorInjected {
              @Inject public TestNonEmptyConstructorInjected(Lazy<String> str, Integer n) {}
            }
           """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Class test.TestNonEmptyConstructorInjected is annotated with @InjectConstructor. "
                    + "Therefore, It must have one unique constructor and it should not be annotated with @Inject."
            )
    }

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldFail_multipleConstructors() {
        val source = javaSource(
            "TestMultipleConstructors",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.InjectConstructor;
            import toothpick.Lazy;
            @InjectConstructor
            public class TestMultipleConstructors {
              public TestMultipleConstructors(Lazy<String> str, Integer n) {}
              public TestMultipleConstructors() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Class test.TestMultipleConstructors is annotated with @InjectConstructor. "
                    + "Therefore, It must have one unique constructor and it should not be annotated with @Inject."
            )
    }
}