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

import org.junit.Test
import toothpick.compiler.factory.FactoryProcessorProvider
import toothpick.compiler.memberinjector.MemberInjectorProcessorProvider

@Suppress("PrivatePropertyName")
class RelaxedFactoryForInjectConstructorTest {

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldWork_whenDefaultConstructorIsPresent_java() {
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
    fun testOptimisticFactoryCreationForInjectConstructor_shouldWork_whenDefaultConstructorIsPresent_kt() {
        val source = ktSource(
            "TestOptimisticFactoryCreationForInjectConstructor",
            """
            package test
            import toothpick.InjectConstructor
            @InjectConstructor
            class TestOptimisticFactoryCreationForInjectConstructor
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
    fun testOptimisticFactoryCreationForInjectConstructor_shouldUse_uniqueConstructorWhenAnnotated_java() {
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

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testOptimisticFactoryCreationForInjectConstructor_shouldUse_uniqueConstructorWhenAnnotated_expected
            )
    }

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldUse_uniqueConstructorWhenAnnotated_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import toothpick.InjectConstructor
            import toothpick.Lazy
            @InjectConstructor
            class TestNonEmptyConstructor(str: Lazy<String>, n: Int)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testOptimisticFactoryCreationForInjectConstructor_shouldUse_uniqueConstructorWhenAnnotated_expected
            )
    }

    private val testOptimisticFactoryCreationForInjectConstructor_shouldUse_uniqueConstructorWhenAnnotated_expected =
        expectedKtSource(
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
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              @Suppress(
                "UNCHECKED_CAST",
                "NAME_SHADOWING"
              )
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1 = scope.getLazy(String::class.java) as Lazy<String>
                val param2 = scope.getInstance(Int::class.java) as Int
                return TestNonEmptyConstructor(param1, param2)
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

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldHave_referenceToMemberInjector_java() {
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

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testOptimisticFactoryCreationForInjectConstructor_shouldHave_referenceToMemberInjector_expected
            )
    }

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldHave_referenceToMemberInjector_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import toothpick.InjectConstructor
            import javax.inject.Inject
            import toothpick.Lazy
            @InjectConstructor
            class TestNonEmptyConstructor(str: Lazy<String>, n: Int) {
              @Inject lateinit var string: String
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testOptimisticFactoryCreationForInjectConstructor_shouldHave_referenceToMemberInjector_expected
            )
    }

    private val testOptimisticFactoryCreationForInjectConstructor_shouldHave_referenceToMemberInjector_expected =
        expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Lazy
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              private val memberInjector: MemberInjector<TestNonEmptyConstructor> =
                  TestNonEmptyConstructor__MemberInjector()
            
              @Suppress(
                "UNCHECKED_CAST",
                "NAME_SHADOWING"
              )
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1 = scope.getLazy(String::class.java) as Lazy<String>
                val param2 = scope.getInstance(Int::class.java) as Int
                return TestNonEmptyConstructor(param1, param2)
                .apply {
                  memberInjector.inject(this, scope)
                }
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

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldFail_uniqueConstructorIsAnnotated_java() {
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
            .withLogContaining(
                "Class test.TestNonEmptyConstructorInjected is annotated with @InjectConstructor. " +
                    "Therefore, It must have one unique constructor and it should not be annotated with @Inject."
            )
    }

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldFail_uniqueConstructorIsAnnotated_kt() {
        val source = ktSource(
            "TestNonEmptyConstructorInjected",
            """
            package test
            import javax.inject.Inject
            import toothpick.InjectConstructor
            import toothpick.Lazy
            @InjectConstructor
            class TestNonEmptyConstructorInjected @Inject constructor(str: Lazy<String>, n: Int)
           """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Class test.TestNonEmptyConstructorInjected is annotated with @InjectConstructor. " +
                    "Therefore, It must have one unique constructor and it should not be annotated with @Inject."
            )
    }

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldFail_multipleConstructors_java() {
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
            .withLogContaining(
                "Class test.TestMultipleConstructors is annotated with @InjectConstructor. " +
                    "Therefore, It must have one unique constructor and it should not be annotated with @Inject."
            )
    }

    @Test
    fun testOptimisticFactoryCreationForInjectConstructor_shouldFail_multipleConstructors_kt() {
        val source = ktSource(
            "TestMultipleConstructors",
            """
            package test
            import javax.inject.Inject
            import toothpick.InjectConstructor
            import toothpick.Lazy
            @InjectConstructor
            class TestMultipleConstructors() {
                constructor(str: Lazy<String>, n: Int)
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Class test.TestMultipleConstructors is annotated with @InjectConstructor. " +
                    "Therefore, It must have one unique constructor and it should not be annotated with @Inject."
            )
    }
}
