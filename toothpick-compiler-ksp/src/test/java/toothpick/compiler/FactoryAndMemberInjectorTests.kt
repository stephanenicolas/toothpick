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

import org.junit.Ignore
import org.junit.Test
import toothpick.compiler.factory.FactoryProcessorProvider
import toothpick.compiler.memberinjector.MemberInjectorProcessorProvider

@Suppress("PrivatePropertyName")
class FactoryAndMemberInjectorTests {

    @Test
    fun testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_java() {
        val source = javaSource(
            "TestAClassThatNeedsInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestAClassThatNeedsInjection {
            @Inject String s;
              @Inject public TestAClassThatNeedsInjection() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_expected
            )
    }

    @Test
    fun testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_kt() {
        val source = ktSource(
            "TestAClassThatNeedsInjection",
            """
            package test
            import javax.inject.Inject
            class TestAClassThatNeedsInjection @Inject constructor() {
                @Inject lateinit var s: String
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_expected
            )
    }

    private val testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_expected =
        expectedKtSource(
            "test/TestAClassThatNeedsInjection__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestAClassThatNeedsInjection__Factory : Factory<TestAClassThatNeedsInjection> {
              private val memberInjector: MemberInjector<TestAClassThatNeedsInjection> =
                  TestAClassThatNeedsInjection__MemberInjector()
            
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestAClassThatNeedsInjection {
                val scope = getTargetScope(scope)
                return TestAClassThatNeedsInjection()
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
    @Ignore("https://github.com/tschuchortdev/kotlin-compile-testing/issues/105")
    fun testAInnerClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_java() {
        val source = javaSource(
            "TestAInnerClassThatNeedsInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestAInnerClassThatNeedsInjection {
              public static class InnerClass  {
                @Inject String s;
                public InnerClass() {}
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAInnerClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_expected
            )
    }

    @Test
    fun testAInnerClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_kt() {
        val source = ktSource(
            "TestAInnerClassThatNeedsInjection",
            """
            package test
            import javax.inject.Inject
            class TestAInnerClassThatNeedsInjection {
              class InnerClass @Inject constructor() {
                @Inject lateinit var s: String
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAInnerClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_expected
            )
    }

    @Suppress("RemoveRedundantBackticks")
    private val testAInnerClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField_expected =
        expectedKtSource(
            "test/TestAInnerClassThatNeedsInjection\$InnerClass__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class `TestAInnerClassThatNeedsInjection${'$'}InnerClass__Factory` :
                Factory<TestAInnerClassThatNeedsInjection.InnerClass> {
              private val memberInjector: MemberInjector<TestAInnerClassThatNeedsInjection.InnerClass> =
                  `TestAInnerClassThatNeedsInjection${'$'}InnerClass__MemberInjector`()
            
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestAInnerClassThatNeedsInjection.InnerClass {
                val scope = getTargetScope(scope)
                return TestAInnerClassThatNeedsInjection.InnerClass()
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
    fun testAInnerClassThatNeedsInjection_shouldFail_whenItIsNotStatic_kt() {
        val source = ktSource(
            "TestAInnerClassThatNeedsInjection",
            """
            package test
            import javax.inject.Inject
            class TestAInnerClassThatNeedsInjection {
              inner class InnerClass @Inject constructor() {
                @Inject lateinit var s: String
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs("Class test.TestAInnerClassThatNeedsInjection.InnerClass is a non static inner class. @Inject constructors are not allowed in non static inner classes.")
    }

    @Test
    fun testAClassThatInheritFromAnotherClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnAnnotatedConstructor_andShouldUseSuperMemberInjector_java() {
        val source = javaSource(
            "TestAClassThatNeedsInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestAClassThatNeedsInjection extends SuperClassThatNeedsInjection {
              @Inject public TestAClassThatNeedsInjection() {}
            }
            class SuperClassThatNeedsInjection {
              @Inject String s;
              public SuperClassThatNeedsInjection() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassThatInheritFromAnotherClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnAnnotatedConstructor_andShouldUseSuperMemberInjector_expected
            )
    }

    @Test
    fun testAClassThatInheritFromAnotherClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnAnnotatedConstructor_andShouldUseSuperMemberInjector_kt() {
        val source = ktSource(
            "TestAClassThatNeedsInjection",
            """
            package test
            import javax.inject.Inject
            class TestAClassThatNeedsInjection @Inject constructor(): SuperClassThatNeedsInjection()
            class SuperClassThatNeedsInjection {
              @Inject lateinit var s: String
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassThatInheritFromAnotherClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnAnnotatedConstructor_andShouldUseSuperMemberInjector_expected
            )
    }

    private val testAClassThatInheritFromAnotherClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnAnnotatedConstructor_andShouldUseSuperMemberInjector_expected =
        expectedKtSource(
            "test/TestAClassThatNeedsInjection__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestAClassThatNeedsInjection__Factory : Factory<TestAClassThatNeedsInjection> {
              private val memberInjector: MemberInjector<SuperClassThatNeedsInjection> =
                  SuperClassThatNeedsInjection__MemberInjector()
            
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestAClassThatNeedsInjection {
                val scope = getTargetScope(scope)
                return TestAClassThatNeedsInjection()
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
    fun testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedMethod_java() {
        val source = javaSource(
            "TestAClassThatNeedsInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestAClassThatNeedsInjection {
              @Inject public TestAClassThatNeedsInjection() {}
              @Inject public void m(String s) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedMethod_expected
            )
    }

    @Test
    fun testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedMethod_kt() {
        val source = ktSource(
            "TestAClassThatNeedsInjection",
            """
            package test
            import javax.inject.Inject
            class TestAClassThatNeedsInjection @Inject constructor() {
              @Inject fun m(s: String) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedMethod_expected
            )
    }

    private val testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedMethod_expected =
        expectedKtSource(
            "test/TestAClassThatNeedsInjection__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestAClassThatNeedsInjection__Factory : Factory<TestAClassThatNeedsInjection> {
              private val memberInjector: MemberInjector<TestAClassThatNeedsInjection> =
                  TestAClassThatNeedsInjection__MemberInjector()
            
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestAClassThatNeedsInjection {
                val scope = getTargetScope(scope)
                return TestAClassThatNeedsInjection()
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
}
