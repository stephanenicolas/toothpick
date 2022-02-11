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

import org.junit.Ignore
import org.junit.Test
import toothpick.compiler.assertLogs
import toothpick.compiler.common.ToothpickOptions.Companion.AdditionalAnnotationTypes
import toothpick.compiler.compilationAssert
import toothpick.compiler.compilesWithoutError
import toothpick.compiler.expectedKtSource
import toothpick.compiler.failsToCompile
import toothpick.compiler.generatesSources
import toothpick.compiler.javaSource
import toothpick.compiler.ktSource
import toothpick.compiler.processedWith
import toothpick.compiler.that
import toothpick.compiler.withOptions

@Suppress("PrivatePropertyName")
class FactoryTest {

    @Test
    fun testEmptyConstructor_shouldWork_whenConstructorIsPublic_java() {
        val source = javaSource(
            "TestEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            public class TestEmptyConstructor {
              @Inject public TestEmptyConstructor() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testEmptyConstructor_shouldWork_whenConstructorIsPublic_expected)
    }

    @Test
    fun testEmptyConstructor_shouldWork_whenConstructorIsPublic_kt() {
        val source = ktSource(
            "TestEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            class TestEmptyConstructor @Inject constructor()
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testEmptyConstructor_shouldWork_whenConstructorIsPublic_expected)
    }

    private val testEmptyConstructor_shouldWork_whenConstructorIsPublic_expected = expectedKtSource(
        "test/TestEmptyConstructor__Factory",
        """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor = TestEmptyConstructor()
            
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
    fun testEmptyConstructor_shouldWork_whenConstructorIsPackage_java() {
        val source = javaSource(
            "TestEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            public class TestEmptyConstructor {
              @Inject TestEmptyConstructor() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testEmptyConstructor_shouldWork_whenConstructorIsPackage_expected)
    }

    @Test
    fun testEmptyConstructor_shouldWork_whenConstructorIsPackage_kt() {
        val source = ktSource(
            "TestEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            class TestEmptyConstructor @Inject constructor()
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testEmptyConstructor_shouldWork_whenConstructorIsPackage_expected)
    }

    private val testEmptyConstructor_shouldWork_whenConstructorIsPackage_expected = expectedKtSource(
        "test/TestEmptyConstructor__Factory",
        """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor = TestEmptyConstructor()
            
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
    fun testEmptyConstructor_shouldWork_whenConstructorIsProtected_java() {
        val source = javaSource(
            "TestEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            public class TestEmptyConstructor {
              @Inject protected TestEmptyConstructor() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testEmptyConstructor_shouldWork_whenConstructorIsProtected_expected)
    }

    @Test
    fun testEmptyConstructor_shouldWork_whenConstructorIsProtected_kt() {
        val source = ktSource(
            "TestEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            open class TestEmptyConstructor @Inject protected constructor()
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testEmptyConstructor_shouldWork_whenConstructorIsProtected_expected)
    }

    private val testEmptyConstructor_shouldWork_whenConstructorIsProtected_expected = expectedKtSource(
        "test/TestEmptyConstructor__Factory",
        """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor = TestEmptyConstructor()
            
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
    fun testPrivateConstructor_java() {
        val source = javaSource(
            "TestPrivateConstructor",
            """
            package test;
            import javax.inject.Inject;
            public class TestPrivateConstructor {
              @Inject private TestPrivateConstructor() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "@Inject constructors must not be private in class test.TestPrivateConstructor"
            )
    }

    @Test
    fun testPrivateConstructor_kt() {
        val source = ktSource(
            "TestPrivateConstructor",
            """
            package test
            import javax.inject.Inject
            class TestPrivateConstructor @Inject private constructor()
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "@Inject constructors must not be private in class test.TestPrivateConstructor"
            )
    }

    @Test
    fun testInjectedConstructorInPrivateClass_shouldNotAllowInjectionInPrivateClasses_java() {
        val source = javaSource(
            "TestConstructorInPrivateClass",
            """
            package test;
            import javax.inject.Inject;
            class Wrapper {
              private class TestConstructorInPrivateClass {
                @Inject public TestConstructorInPrivateClass() {}
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Class test.Wrapper.TestConstructorInPrivateClass is private. @Inject constructors are not allowed in private classes."
            )
    }

    @Test
    fun testInjectedConstructorInPrivateClass_shouldNotAllowInjectionInPrivateClasses_kt() {
        val source = ktSource(
            "TestConstructorInPrivateClass",
            """
            package test
            import javax.inject.Inject
            class Wrapper {
              private class TestConstructorInPrivateClass @Inject constructor()
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Class test.Wrapper.TestConstructorInPrivateClass is private. @Inject constructors are not allowed in private classes."
            )
    }

    @Test
    @Ignore("https://github.com/tschuchortdev/kotlin-compile-testing/issues/105")
    fun testInjectedConstructorInProtectedClass_shouldWork_java() {
        val source = javaSource(
            "TestConstructorInProtectedClass",
            """
            package test;
            import javax.inject.Inject;
            class Wrapper {
              protected static class TestConstructorInProtectedClass {
                @Inject public TestConstructorInProtectedClass() {}
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testInjectedConstructorInProtectedClass_shouldWork_expected)
    }

    @Test
    fun testInjectedConstructorInProtectedClass_shouldWork_kt() {
        val source = ktSource(
            "TestConstructorInProtectedClass",
            """
            package test
            import javax.inject.Inject
            open class Wrapper {
              protected class TestConstructorInProtectedClass @Inject constructor()
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testInjectedConstructorInProtectedClass_shouldWork_expected)
    }

    @Suppress("RemoveRedundantBackticks")
    private val testInjectedConstructorInProtectedClass_shouldWork_expected = expectedKtSource(
        "test/Wrapper\$TestConstructorInProtectedClass__Factory",
        """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            protected class `Wrapper${'$'}TestConstructorInProtectedClass__Factory` :
                Factory<Wrapper.TestConstructorInProtectedClass> {
              public override fun createInstance(scope: Scope): Wrapper.TestConstructorInProtectedClass =
                  Wrapper.TestConstructorInProtectedClass()
            
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
    fun testInjectedConstructorInPackageClass_shouldWork_java() {
        val source = javaSource(
            "TestConstructorInPackageClass",
            """
            package test;
            import javax.inject.Inject;
            class TestConstructorInPackageClass {
              @Inject public TestConstructorInPackageClass() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testInjectedConstructorInPackageClass_shouldWork_expected)
    }

    @Test
    fun testInjectedConstructorInPackageClass_shouldWork_kt() {
        val source = ktSource(
            "TestConstructorInPackageClass",
            """
            package test
            import javax.inject.Inject
            class TestConstructorInPackageClass @Inject constructor()
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testInjectedConstructorInPackageClass_shouldWork_expected)
    }

    private val testInjectedConstructorInPackageClass_shouldWork_expected = expectedKtSource(
        "test/TestConstructorInPackageClass__Factory",
        """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestConstructorInPackageClass__Factory : Factory<TestConstructorInPackageClass> {
              public override fun createInstance(scope: Scope): TestConstructorInPackageClass =
                  TestConstructorInPackageClass()
            
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
    fun test2InjectedConstructors_java() {
        val source = javaSource(
            "TestPrivateConstructor",
            """
            package test;
            import javax.inject.Inject;
            public class TestPrivateConstructor {
              @Inject private TestPrivateConstructor() {}
              @Inject private TestPrivateConstructor(String s) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Class test.TestPrivateConstructor cannot have more than one @Inject-annotated constructor."
            )
    }

    @Test
    fun test2InjectedConstructors_kt() {
        val source = ktSource(
            "TestPrivateConstructor",
            """
            package test
            import javax.inject.Inject
            class TestPrivateConstructor @Inject private constructor() {
              @Inject private constructor(s: String)
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Class test.TestPrivateConstructor cannot have more than one @Inject-annotated constructor."
            )
    }

    @Test
    fun test2Constructors_butOnlyOneIsInjected_java() {
        val source = javaSource(
            "Test2Constructors",
            """
            package test;
            import javax.inject.Inject;
            public class Test2Constructors {
              @Inject public Test2Constructors() {}
              public Test2Constructors(String s) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(test2Constructors_butOnlyOneIsInjected_expected)
    }

    @Test
    fun test2Constructors_butOnlyOneIsInjected_kt() {
        val source = ktSource(
            "Test2Constructors",
            """
            package test
            import javax.inject.Inject
            class Test2Constructors @Inject constructor() {
              constructor(s: String)
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(test2Constructors_butOnlyOneIsInjected_expected)
    }

    private val test2Constructors_butOnlyOneIsInjected_expected = expectedKtSource(
        "test/Test2Constructors__Factory",
        """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class Test2Constructors__Factory : Factory<Test2Constructors> {
              public override fun createInstance(scope: Scope): Test2Constructors = Test2Constructors()
            
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
    fun testNonEmptyConstructor_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            public class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(String str, Integer n) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNonEmptyConstructor_expected)
    }

    @Test
    fun testNonEmptyConstructor_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            class TestNonEmptyConstructor @Inject constructor(str: String, n: Int)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNonEmptyConstructor_expected)
    }

    private val testNonEmptyConstructor_expected = expectedKtSource(
        "test/TestNonEmptyConstructor__Factory",
        """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1 = scope.getInstance(String::class.java) as String
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
    fun testNonEmptyConstructorWithLazy_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(Lazy<String> str, Integer n) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNonEmptyConstructorWithLazy_expected)
    }

    @Test
    fun testNonEmptyConstructorWithLazy_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestNonEmptyConstructor @Inject constructor(str: Lazy<String>, n: Int)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNonEmptyConstructorWithLazy_expected)
    }

    private val testNonEmptyConstructorWithLazy_expected = expectedKtSource(
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
              @Suppress("NAME_SHADOWING")
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
    fun testNonEmptyConstructorWithProvider_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(Provider<String> str, Integer n) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNonEmptyConstructorWithProvider_expected)
    }

    @Test
    fun testNonEmptyConstructorWithProvider_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Provider
            class TestNonEmptyConstructor @Inject constructor(str: Provider<String>, n: Int)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNonEmptyConstructorWithProvider_expected)
    }

    private val testNonEmptyConstructorWithProvider_expected = expectedKtSource(
        "test/TestNonEmptyConstructor__Factory",
        """
            package test
            
            import javax.inject.Provider
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1 = scope.getProvider(String::class.java) as Provider<String>
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
    fun testNonEmptyConstructor_shouldFail_whenContainsInvalidLazyParameter_java() {
        @Suppress("rawtypes")
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(Lazy lazy, Integer n) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of lazy is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testNonEmptyConstructor_shouldFail_whenContainsInvalidLazyParameter_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestNonEmptyConstructor @Inject constructor(lazy: Lazy<*>, n: Int)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of lazy is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testNonEmptyConstructor_shouldFail_whenContainsInvalidProviderParameter_java() {
        @Suppress("rawtypes")
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(Provider provider, Integer n) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of provider is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testNonEmptyConstructor_shouldFail_whenContainsInvalidProviderParameter_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Provider
            class TestNonEmptyConstructor @Inject constructor(provider: Provider<*>, n: Int)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of provider is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testNonEmptyConstructorWithGenerics_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import java.util.List;
            import javax.inject.Inject;
            public class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(List<String> str) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNonEmptyConstructorWithGenerics_expected)
    }

    @Test
    fun testNonEmptyConstructorWithGenerics_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import kotlin.collections.MutableList
            import javax.inject.Inject
            class TestNonEmptyConstructor @Inject constructor(str: MutableList<String>)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNonEmptyConstructorWithGenerics_expected)
    }

    private val testNonEmptyConstructorWithGenerics_expected = expectedKtSource(
        "test/TestNonEmptyConstructor__Factory",
        """
            package test
            
            import kotlin.Boolean
            import kotlin.String
            import kotlin.Suppress
            import kotlin.collections.MutableList
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1 = scope.getInstance(MutableList::class.java) as MutableList<String>
                return TestNonEmptyConstructor(param1)
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
    fun testNonEmptyConstructorWithLazyAndGenerics_shouldFail_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import java.util.List;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(Lazy<List<String>> str) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "str is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testNonEmptyConstructorWithLazyAndGenerics_shouldFail_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import java.util.List
            import javax.inject.Inject
            import toothpick.Lazy
            class TestNonEmptyConstructor @Inject constructor(str: Lazy<List<String>>)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "str is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testNonEmptyConstructorWithProviderAndGenerics_shouldFail_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import java.util.List;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(Provider<List<String>> str) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "str is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testNonEmptyConstructorWithProviderAndGenerics_shouldFail_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import java.util.List
            import javax.inject.Inject
            import javax.inject.Provider
            class TestNonEmptyConstructor @Inject constructor(str: Provider<List<String>>)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "str is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testAbstractClassWithInjectedConstructor_java() {
        val source = javaSource(
            "TestInvalidClassConstructor",
            """
            package test;
            import javax.inject.Inject;
            public abstract class TestInvalidClassConstructor {
              @Inject public TestInvalidClassConstructor() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "The class test.TestInvalidClassConstructor is abstract or private. It cannot have an injected constructor."
            )
    }

    @Test
    fun testAbstractClassWithInjectedConstructor_kt() {
        val source = ktSource(
            "TestInvalidClassConstructor",
            """
            package test
            import javax.inject.Inject
            abstract class TestInvalidClassConstructor @Inject constructor()
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "The class test.TestInvalidClassConstructor is abstract or private. It cannot have an injected constructor."
            )
    }

    @Test
    fun testClassWithInjectedConstructorThrowingException_java() {
        @Suppress("RedundantThrows")
        val source = javaSource(
            "TestClassConstructorThrowingException",
            """
            package test;
            import javax.inject.Inject;
            public class TestClassConstructorThrowingException {
              @Inject public TestClassConstructorThrowingException(String s) throws Exception {}
            }
            """
        )

        val expected = expectedKtSource(
            "test/TestClassConstructorThrowingException__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestClassConstructorThrowingException__Factory :
                Factory<TestClassConstructorThrowingException> {
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestClassConstructorThrowingException {
                val scope = getTargetScope(scope)
                val param1 = scope.getInstance(String::class.java) as String
                return TestClassConstructorThrowingException(param1)
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
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expected)
    }

    @Test
    fun testAClassWithSingletonAnnotation_shouldHaveAFactoryThatSaysItIsASingleton_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Singleton;
            @Singleton
            public final class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(String str, Integer n) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testAClassWithSingletonAnnotation_shouldHaveAFactoryThatSaysItIsASingleton_expected)
    }

    @Test
    fun testAClassWithSingletonAnnotation_shouldHaveAFactoryThatSaysItIsASingleton_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Singleton
            @Singleton
            class TestNonEmptyConstructor @Inject constructor(str: String, n: Int)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testAClassWithSingletonAnnotation_shouldHaveAFactoryThatSaysItIsASingleton_expected)
    }

    private val testAClassWithSingletonAnnotation_shouldHaveAFactoryThatSaysItIsASingleton_expected = expectedKtSource(
        "test/TestNonEmptyConstructor__Factory",
        """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1 = scope.getInstance(String::class.java) as String
                val param2 = scope.getInstance(Int::class.java) as Int
                return TestNonEmptyConstructor(param1, param2)
              }
            
              public override fun getTargetScope(scope: Scope): Scope = scope.rootScope
            
              public override fun hasScopeAnnotation(): Boolean = true
            
              public override fun hasSingletonAnnotation(): Boolean = true
            
              public override fun hasReleasableAnnotation(): Boolean = false
            
              public override fun hasProvidesSingletonAnnotation(): Boolean = false
            
              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            """
    )

    @Test
    fun testAClassWithSingletonAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsASingleton_java() {
        val source = javaSource(
            "TestEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Singleton;
            @Singleton
            public final class TestEmptyConstructor {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassWithSingletonAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsASingleton_expected
            )
    }

    @Test
    fun testAClassWithSingletonAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsASingleton_kt() {
        val source = ktSource(
            "TestEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Singleton
            @Singleton
            class TestEmptyConstructor
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassWithSingletonAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsASingleton_expected
            )
    }

    private val testAClassWithSingletonAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsASingleton_expected =
        expectedKtSource(
            "test/TestEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor = TestEmptyConstructor()
            
              public override fun getTargetScope(scope: Scope): Scope = scope.rootScope
            
              public override fun hasScopeAnnotation(): Boolean = true
            
              public override fun hasSingletonAnnotation(): Boolean = true
            
              public override fun hasReleasableAnnotation(): Boolean = false
            
              public override fun hasProvidesSingletonAnnotation(): Boolean = false
            
              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            """
        )

    @Test
    fun testAClassWithEmptyScopedAnnotation_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
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
            public final class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(String str, Integer n) {}
              public @interface FooScope {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassWithEmptyScopedAnnotation_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_expected
            )
    }

    @Test
    fun testAClassWithEmptyScopedAnnotation_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Scope
            @Scope
            @Retention(AnnotationRetention.RUNTIME)
            annotation class CustomScope
            @CustomScope
            class TestNonEmptyConstructor @Inject constructor(str: String, n: Int) {
              annotation class FooScope
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassWithEmptyScopedAnnotation_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_expected
            )
    }

    private val testAClassWithEmptyScopedAnnotation_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_expected =
        expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1 = scope.getInstance(String::class.java) as String
                val param2 = scope.getInstance(Int::class.java) as Int
                return TestNonEmptyConstructor(param1, param2)
              }
            
              public override fun getTargetScope(scope: Scope): Scope =
                  scope.getParentScope(CustomScope::class.java)
            
              public override fun hasScopeAnnotation(): Boolean = true
            
              public override fun hasSingletonAnnotation(): Boolean = false
            
              public override fun hasReleasableAnnotation(): Boolean = false
            
              public override fun hasProvidesSingletonAnnotation(): Boolean = false
            
              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            """
        )

    @Test
    fun testAClassWithEmptyScopedAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_java() {
        val source = javaSource(
            "TestEmptyConstructor",
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
            public final class TestEmptyConstructor {
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .withOptions(AdditionalAnnotationTypes to "test.CustomScope")
            .compilesWithoutError()
            .generatesSources(
                testAClassWithEmptyScopedAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_expected
            )
    }

    @Test
    fun testAClassWithEmptyScopedAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_kt() {
        val source = ktSource(
            "TestEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Scope
            @Scope
            @Retention(AnnotationRetention.RUNTIME)
            annotation class CustomScope
            @CustomScope
            class TestEmptyConstructor
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .withOptions(AdditionalAnnotationTypes to "test.CustomScope")
            .compilesWithoutError()
            .generatesSources(
                testAClassWithEmptyScopedAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_expected
            )
    }

    private val testAClassWithEmptyScopedAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope_expected =
        expectedKtSource(
            "test/TestEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor = TestEmptyConstructor()
            
              public override fun getTargetScope(scope: Scope): Scope =
                  scope.getParentScope(CustomScope::class.java)
            
              public override fun hasScopeAnnotation(): Boolean = true
            
              public override fun hasSingletonAnnotation(): Boolean = false
            
              public override fun hasReleasableAnnotation(): Boolean = false
            
              public override fun hasProvidesSingletonAnnotation(): Boolean = false
            
              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            """
        )

    @Test
    fun testAClassWithScopedAnnotationAndSingleton_shouldHaveAFactoryThatSaysItIsScopedInCurrentScopeAndSingleton_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Scope;
            import javax.inject.Singleton;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            @Scope
            @Retention(RetentionPolicy.RUNTIME)
            @interface CustomScope {}
            @CustomScope @Singleton
            public final class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(String str, Integer n) {}
              public @interface FooScope {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassWithScopedAnnotationAndSingleton_shouldHaveAFactoryThatSaysItIsScopedInCurrentScopeAndSingleton_expected
            )
    }

    @Test
    fun testAClassWithScopedAnnotationAndSingleton_shouldHaveAFactoryThatSaysItIsScopedInCurrentScopeAndSingleton_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Scope
            import javax.inject.Singleton
            @Scope
            @Retention(AnnotationRetention.RUNTIME)
            annotation class CustomScope
            @CustomScope @Singleton
            class TestNonEmptyConstructor @Inject constructor(str: String, n: Int) {
              annotation class FooScope
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassWithScopedAnnotationAndSingleton_shouldHaveAFactoryThatSaysItIsScopedInCurrentScopeAndSingleton_expected
            )
    }

    private val testAClassWithScopedAnnotationAndSingleton_shouldHaveAFactoryThatSaysItIsScopedInCurrentScopeAndSingleton_expected =
        expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1 = scope.getInstance(String::class.java) as String
                val param2 = scope.getInstance(Int::class.java) as Int
                return TestNonEmptyConstructor(param1, param2)
              }
            
              public override fun getTargetScope(scope: Scope): Scope =
                  scope.getParentScope(CustomScope::class.java)
            
              public override fun hasScopeAnnotation(): Boolean = true
            
              public override fun hasSingletonAnnotation(): Boolean = true
            
              public override fun hasReleasableAnnotation(): Boolean = false
            
              public override fun hasProvidesSingletonAnnotation(): Boolean = false
            
              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            """
        )

    @Test
    fun testAClassWithProvidesSingletonAnnotation_shouldHaveAFactoryThatSaysItIsAProvidesSingleton_java() {
        val source = javaSource(
            "TestNonEmptyConstructor",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Singleton;
            import toothpick.ProvidesSingleton;
            @ProvidesSingleton @Singleton
            public class TestNonEmptyConstructor {
              @Inject public TestNonEmptyConstructor(String str, Integer n) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassWithProvidesSingletonAnnotation_shouldHaveAFactoryThatSaysItIsAProvidesSingleton_expected
            )
    }

    @Test
    fun testAClassWithProvidesSingletonAnnotation_shouldHaveAFactoryThatSaysItIsAProvidesSingleton_kt() {
        val source = ktSource(
            "TestNonEmptyConstructor",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Singleton
            import toothpick.ProvidesSingleton
            @ProvidesSingleton @Singleton
            class TestNonEmptyConstructor @Inject constructor(str: String, n: Int)
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testAClassWithProvidesSingletonAnnotation_shouldHaveAFactoryThatSaysItIsAProvidesSingleton_expected
            )
    }

    private val testAClassWithProvidesSingletonAnnotation_shouldHaveAFactoryThatSaysItIsAProvidesSingleton_expected =
        expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier"
            )
            public class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1 = scope.getInstance(String::class.java) as String
                val param2 = scope.getInstance(Int::class.java) as Int
                return TestNonEmptyConstructor(param1, param2)
              }
            
              public override fun getTargetScope(scope: Scope): Scope = scope.rootScope
            
              public override fun hasScopeAnnotation(): Boolean = true
            
              public override fun hasSingletonAnnotation(): Boolean = true
            
              public override fun hasReleasableAnnotation(): Boolean = false
            
              public override fun hasProvidesSingletonAnnotation(): Boolean = true
            
              public override fun hasProvidesReleasableAnnotation(): Boolean = false
            }
            """
        )

    @Test
    @Ignore("KSP does not support checking if type is a Java primitive")
    fun testInjectedConstructor_withPrimitiveParam_java() {
        val source = javaSource(
            "TestPrimitiveConstructor",
            """
            package test;
            import javax.inject.Inject;
            public class TestPrimitiveConstructor {
              @Inject TestPrimitiveConstructor(int n) {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Parameter n in method/constructor test.TestPrimitiveConstructor#<init> is of type int which is not supported by Toothpick."
            )
    }
}
