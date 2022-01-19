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
import toothpick.compiler.processedWith
import toothpick.compiler.that
import toothpick.compiler.withOptions

class FactoryTest {

    @Test
    fun testEmptyConstructor_shouldWork_whenConstructorIsPublic() {
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

        val expectedSource = expectedKtSource(
            "test/TestEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor {
                val testEmptyConstructor: TestEmptyConstructor = TestEmptyConstructor()
                return testEmptyConstructor
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
            .generatesSources(expectedSource)
    }

    @Test
    fun testEmptyConstructor_shouldWork_whenConstructorIsPackage() {
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

        val expectedSource = expectedKtSource(
            "test/TestEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor {
                val testEmptyConstructor: TestEmptyConstructor = TestEmptyConstructor()
                return testEmptyConstructor
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
            .generatesSources(expectedSource)
    }

    @Test
    fun testEmptyConstructor_shouldWork_whenConstructorIsProtected() {
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

        val expectedSource = expectedKtSource(
            "test/TestEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor {
                val testEmptyConstructor: TestEmptyConstructor = TestEmptyConstructor()
                return testEmptyConstructor
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
            .generatesSources(expectedSource)
    }

    @Test
    fun testPrivateConstructor() {
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
    fun testInjectedConstructorInPrivateClass_shouldNotAllowInjectionInPrivateClasses() {
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
    fun testInjectedConstructorInProtectedClass_shouldWork() {
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

        val expectedSource = expectedKtSource(
            "test/Wrapper\$TestConstructorInProtectedClass__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class Wrapper${'$'}TestConstructorInProtectedClass__Factory implements Factory<Wrapper.TestConstructorInProtectedClass> {
              @Override
              public Wrapper.TestConstructorInProtectedClass createInstance(Scope scope) {
                Wrapper.TestConstructorInProtectedClass testConstructorInProtectedClass = new Wrapper.TestConstructorInProtectedClass();
                return testConstructorInProtectedClass;
              }
            
              @Override
              public Scope getTargetScope(Scope scope) {
                return scope;
              }
            
              @Override
              public boolean hasScopeAnnotation() {
                return false;
              }
            
              @Override
              public boolean hasSingletonAnnotation() {
                return false;
              }
            
              @Override
              public boolean hasReleasableAnnotation() {
                return false;
              }
            
              @Override
              public boolean hasProvidesSingletonAnnotation() {
                return false;
              }
            
              @Override
              public boolean hasProvidesReleasableAnnotation() {
                return false;
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testInjectedConstructorInPackageClass_shouldWork() {
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

        val expectedSource = expectedKtSource(
            "test/TestConstructorInPackageClass__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestConstructorInPackageClass__Factory : Factory<TestConstructorInPackageClass> {
              public override fun createInstance(scope: Scope): TestConstructorInPackageClass {
                val testConstructorInPackageClass: TestConstructorInPackageClass =
                    TestConstructorInPackageClass()
                return testConstructorInPackageClass
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
            .generatesSources(expectedSource)
    }

    @Test
    fun test2InjectedConstructors() {
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
    fun test2Constructors_butOnlyOneIsInjected() {
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

        val expectedSource = expectedKtSource(
            "test/Test2Constructors__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class Test2Constructors__Factory : Factory<Test2Constructors> {
              public override fun createInstance(scope: Scope): Test2Constructors {
                val test2Constructors: Test2Constructors = Test2Constructors()
                return test2Constructors
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
            .generatesSources(expectedSource)
    }

    @Test
    fun testNonEmptyConstructor() {
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

        val expectedSource = expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1: String = scope.getInstance(String::class.java)
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
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testNonEmptyConstructorWithLazy() {
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
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testNonEmptyConstructorWithProvider() {
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

        val expectedSource = expectedKtSource(
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
            
            @Suppress("ClassName")
            internal class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1: Provider<String> = scope.getProvider(String::class.java)
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
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testNonEmptyConstructor_shouldFail_whenContainsInvalidLazyParameter() {
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
                "Type of test.TestNonEmptyConstructor.<init> is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testNonEmptyConstructor_shouldFail_whenContainsInvalidProviderParameter() {
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
                "Type of test.TestNonEmptyConstructor.<init> is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testNonEmptyConstructorWithGenerics() {
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

        val expectedSource = expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import kotlin.collections.MutableList
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1: MutableList<*> = scope.getInstance(MutableList::class.java)
                val testNonEmptyConstructor: TestNonEmptyConstructor = TestNonEmptyConstructor(param1)
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
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testNonEmptyConstructorWithLazyAndGenerics_shouldFail() {
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
                "Lazy/Provider is not valid in test.TestNonEmptyConstructor.<init>. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testNonEmptyConstructorWithProviderAndGenerics_shouldFail() {
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
                "Lazy/Provider is not valid in test.TestNonEmptyConstructor.<init>. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testAbstractClassWithInjectedConstructor() {
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
    fun testClassWithInjectedConstructorThrowingException() {
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

        val expectedSource = expectedKtSource(
            "test/TestClassConstructorThrowingException__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestClassConstructorThrowingException__Factory :
                Factory<TestClassConstructorThrowingException> {
              public override fun createInstance(scope: Scope): TestClassConstructorThrowingException {
                val scope = getTargetScope(scope)
                val param1: String = scope.getInstance(String::class.java)
                val testClassConstructorThrowingException: TestClassConstructorThrowingException =
                    TestClassConstructorThrowingException(param1)
                return testClassConstructorThrowingException
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
            .generatesSources(expectedSource)
    }

    @Test
    fun testAClassWithSingletonAnnotation_shouldHaveAFactoryThatSaysItIsASingleton() {
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

        val expectedSource = expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1: String = scope.getInstance(String::class.java)
                val param2: Int = scope.getInstance(Int::class.java)
                val testNonEmptyConstructor: TestNonEmptyConstructor = TestNonEmptyConstructor(param1, param2)
                return testNonEmptyConstructor
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

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testAClassWithSingletonAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsASingleton() {
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

        val expectedSource = expectedKtSource(
            "test/TestEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor {
                val testEmptyConstructor: TestEmptyConstructor = TestEmptyConstructor()
                return testEmptyConstructor
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

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testAClassWithEmptyScopedAnnotation_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope() {
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

        val expectedSource = expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1: String = scope.getInstance(String::class.java)
                val param2: Int = scope.getInstance(Int::class.java)
                val testNonEmptyConstructor: TestNonEmptyConstructor = TestNonEmptyConstructor(param1, param2)
                return testNonEmptyConstructor
              }
            
              public override fun getTargetScope(scope: Scope): Scope =
                  scope.getParentScope(test.CustomScope::class.java)
            
              public override fun hasScopeAnnotation(): Boolean = true
            
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
            .generatesSources(expectedSource)
    }

    @Test
    fun testAClassWithEmptyScopedAnnotationAndNoConstructor_shouldHaveAFactoryThatSaysItIsScopedInCurrentScope() {
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

        val expectedSource = expectedKtSource(
            "test/TestEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestEmptyConstructor__Factory : Factory<TestEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestEmptyConstructor {
                val testEmptyConstructor: TestEmptyConstructor = TestEmptyConstructor()
                return testEmptyConstructor
              }
            
              public override fun getTargetScope(scope: Scope): Scope =
                  scope.getParentScope(test.CustomScope::class.java)
            
              public override fun hasScopeAnnotation(): Boolean = true
            
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
            .withOptions(AdditionalAnnotationTypes to "test.CustomScope")
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testAClassWithScopedAnnotationAndSingleton_shouldHaveAFactoryThatSaysItIsScopedInCurrentScopeAndSingleton() {
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

        val expectedSource = expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1: String = scope.getInstance(String::class.java)
                val param2: Int = scope.getInstance(Int::class.java)
                val testNonEmptyConstructor: TestNonEmptyConstructor = TestNonEmptyConstructor(param1, param2)
                return testNonEmptyConstructor
              }
            
              public override fun getTargetScope(scope: Scope): Scope =
                  scope.getParentScope(test.CustomScope::class.java)
            
              public override fun hasScopeAnnotation(): Boolean = true
            
              public override fun hasSingletonAnnotation(): Boolean = true
            
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
            .generatesSources(expectedSource)
    }

    @Test
    fun testAClassWithProvidesSingletonAnnotation_shouldHaveAFactoryThatSaysItIsAProvidesSingleton() {
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

        val expectedSource = expectedKtSource(
            "test/TestNonEmptyConstructor__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Int
            import kotlin.String
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestNonEmptyConstructor__Factory : Factory<TestNonEmptyConstructor> {
              public override fun createInstance(scope: Scope): TestNonEmptyConstructor {
                val scope = getTargetScope(scope)
                val param1: String = scope.getInstance(String::class.java)
                val param2: Int = scope.getInstance(Int::class.java)
                val testNonEmptyConstructor: TestNonEmptyConstructor = TestNonEmptyConstructor(param1, param2)
                return testNonEmptyConstructor
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

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    @Ignore("KSP does not support checking if type is a Java primitive")
    fun testInjectedConstructor_withPrimitiveParam() {
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
