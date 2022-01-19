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
package toothpick.compiler.memberinjector

import org.junit.Test
import toothpick.compiler.assertLogs
import toothpick.compiler.compilationAssert
import toothpick.compiler.compilesWithoutError
import toothpick.compiler.expectedKtSource
import toothpick.compiler.failsToCompile
import toothpick.compiler.generatesSources
import toothpick.compiler.javaSource
import toothpick.compiler.processedWith
import toothpick.compiler.that

class MethodMemberInjectorTest {

    @Test
    fun testSimpleMethodInjection() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestMethodInjection {
              @Inject
              public void m(Foo foo) {}
            }
            class Foo {}
            """
        )

        val expectedSource = expectedKtSource(
            "test/TestMethodInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1: Foo = scope.getInstance(Foo::class.java)
                target.m(param1)
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testSimpleMethodInjectionWithLazy() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestMethodInjection {
              @Inject
              public void m(Lazy<Foo> foo) {}
            }
            class Foo {}
            """
        )

        val expectedSource = expectedKtSource(
            "test/TestMethodInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.Lazy
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1: Lazy<Foo> = scope.getLazy(Foo::class.java)
                target.m(param1)
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testSimpleMethodInjectionWithProvider() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestMethodInjection {
              @Inject
              public void m(Provider<Foo> foo) {}
            }
            class Foo {}
            """
        )

        val expectedSource = expectedKtSource(
            "test/TestMethodInjection__MemberInjector",
            """
            package test
            
            import javax.inject.Provider
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1: Provider<Foo> = scope.getProvider(Foo::class.java)
                target.m(param1)
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testSimpleMethodInjectionWithLazyOfGenericTypeButNotLazyOfGenericType() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestMethodInjection {
              @Inject
              public void m(Lazy<Foo> foo) {}
            }
            class Foo<T> {}
            """
        )

        val expectedSource = expectedKtSource(
            "test/TestMethodInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.Lazy
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1: Lazy<Foo<*>> = scope.getLazy(Foo::class.java)
                target.m(param1)
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testSimpleMethodInjectionWithLazyOfGenericType_shouldFail_WithLazyOfGenericType() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestMethodInjection {
              @Inject
              public void m(Lazy<Foo<String>> foo) {}
            }
            class Foo<T> {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Lazy/Provider is not valid in test.TestMethodInjection.m. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenInjectedMethodIsPrivate() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestMethodInjection {
              @Inject
              private void m(Foo foo) {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "@Inject-annotated methods must not be private: test.TestMethodInjection.m"
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenContainingClassIsPrivate() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestMethodInjection {
              private static class InnerClass {
                @Inject
                public void m(Foo foo) {}
              }
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "@Injected test.TestMethodInjection.InnerClass.m; the parent class must not be private."
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenInjectedMethodParameterIsInvalidLazy() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestMethodInjection {
              @Inject
              public void m(Lazy foo) {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of test.TestMethodInjection.m is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenInjectedMethodParameterIsInvalidProvider() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestMethodInjection {
              @Inject
              public void m(Provider foo) {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of test.TestMethodInjection.m is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testOverrideMethodInjection() {
        val source = javaSource(
            "TestMethodInjectionParent",
            """
            package test;
            import javax.inject.Inject;
            public class TestMethodInjectionParent {
              @Inject
              public void m(Foo foo) {}
              public static class TestMethodInjection extends TestMethodInjectionParent {
                @Inject
                public void m(Foo foo) {}
              }
            }
            class Foo {}
            """
        )

        val expectedSource = expectedKtSource(
            "test/TestMethodInjectionParent\$TestMethodInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class `TestMethodInjectionParent${'$'}TestMethodInjection__MemberInjector` :
                MemberInjector<TestMethodInjectionParent.TestMethodInjection> {
              private val superMemberInjector: MemberInjector<TestMethodInjectionParent> =
                  TestMethodInjectionParent__MemberInjector()
            
              public override fun inject(target: TestMethodInjectionParent.TestMethodInjection, scope: Scope):
                  Unit {
                superMemberInjector.inject(target, scope)
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testMethodInjection_withException() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestMethodInjection {
              @Inject
              public void m(Foo foo) throws Exception {}
            }
            class Foo {}
            """
        )

        val expectedSource = expectedKtSource(
            "test/TestMethodInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1: Foo = scope.getInstance(Foo::class.java)
                target.m(param1)
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testMethodInjection_withExceptions() {
        val source = javaSource(
            "TestMethodInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestMethodInjection {
              @Inject
              public void m(Foo foo) throws Exception, Throwable {}
            }
            class Foo {}
            """
        )

        val expectedSource = expectedKtSource(
            "test/TestMethodInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1: Foo = scope.getInstance(Foo::class.java)
                target.m(param1)
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }
}
