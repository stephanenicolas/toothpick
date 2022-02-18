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
import toothpick.compiler.compilationAssert
import toothpick.compiler.compilesWithoutError
import toothpick.compiler.expectedKtSource
import toothpick.compiler.failsToCompile
import toothpick.compiler.generatesSources
import toothpick.compiler.javaSource
import toothpick.compiler.ktSource
import toothpick.compiler.processedWith
import toothpick.compiler.that
import toothpick.compiler.withLogContaining

@Suppress("PrivatePropertyName")
class MethodMemberInjectorTest {

    @Test
    fun testSimpleMethodInjection_java() {
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

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testSimpleMethodInjection_expected)
    }

    @Test
    fun testSimpleMethodInjection_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            class TestMethodInjection {
              @Inject
              fun m(foo: Foo) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testSimpleMethodInjection_expected)
    }

    private val testSimpleMethodInjection_expected = expectedKtSource(
        "test/TestMethodInjection__MemberInjector",
        """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier",
              "UNCHECKED_CAST"
            )
            public class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1 = scope.getInstance(Foo::class.java) as Foo
                target.m(param1)
              }
            }
            """
    )

    @Test
    fun testSimpleMethodInjectionWithLazy_java() {
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

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testSimpleMethodInjectionWithLazy_expected)
    }

    @Test
    fun testSimpleMethodInjectionWithLazy_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestMethodInjection {
              @Inject
              fun m(foo: Lazy<Foo>) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testSimpleMethodInjectionWithLazy_expected)
    }

    private val testSimpleMethodInjectionWithLazy_expected = expectedKtSource(
        "test/TestMethodInjection__MemberInjector",
        """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.Lazy
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier",
              "UNCHECKED_CAST"
            )
            public class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1 = scope.getLazy(Foo::class.java) as Lazy<Foo>
                target.m(param1)
              }
            }
            """
    )

    @Test
    fun testSimpleMethodInjectionWithProvider_java() {
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

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testSimpleMethodInjectionWithProvider_expected)
    }

    @Test
    fun testSimpleMethodInjectionWithProvider_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Provider
            class TestMethodInjection {
              @Inject
              fun m(foo: Provider<Foo>) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testSimpleMethodInjectionWithProvider_expected)
    }

    private val testSimpleMethodInjectionWithProvider_expected = expectedKtSource(
        "test/TestMethodInjection__MemberInjector",
        """
            package test
            
            import javax.inject.Provider
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier",
              "UNCHECKED_CAST"
            )
            public class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1 = scope.getProvider(Foo::class.java) as Provider<Foo>
                target.m(param1)
              }
            }
            """
    )

    @Test
    fun testSimpleMethodInjectionWithLazyOfGenericTypeButNotLazyOfGenericType_java() {
        @Suppress("rawtypes")
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

        val expected = expectedKtSource(
            "test/TestMethodInjection__MemberInjector",
            """
            package test
            
            import kotlin.Any
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.Lazy
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier",
              "UNCHECKED_CAST"
            )
            public class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1 = scope.getLazy(Foo::class.java) as Lazy<Foo<Any>>
                target.m(param1)
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expected)
    }

    @Test
    fun testSimpleMethodInjectionWithLazyOfGenericTypeButNotLazyOfGenericType_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestMethodInjection {
              @Inject
              fun m(foo: Lazy<Foo<*>>) {}
            }
            class Foo<T>
            """
        )

        val expected = expectedKtSource(
            "test/TestMethodInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.Lazy
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier",
              "UNCHECKED_CAST"
            )
            public class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1 = scope.getLazy(Foo::class.java) as Lazy<Foo<*>>
                target.m(param1)
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expected)
    }

    @Test
    fun testSimpleMethodInjectionWithLazyOfGenericType_shouldFail_WithLazyOfGenericType_java() {
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
            .withLogContaining(
                "test.TestMethodInjection.m is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testSimpleMethodInjectionWithLazyOfGenericType_shouldFail_WithLazyOfGenericType_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestMethodInjection {
              @Inject
              fun m(foo: Lazy<Foo<String>>) {}
            }
            class Foo<T>
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "test.TestMethodInjection.m is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenInjectedMethodIsPrivate_java() {
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
            .withLogContaining(
                "@Inject-annotated methods must not be private: test.TestMethodInjection.m"
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenInjectedMethodIsPrivate_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            class TestMethodInjection {
              @Inject
              private fun m(foo: Foo) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Inject-annotated methods must not be private: test.TestMethodInjection.m"
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenContainingClassIsPrivate_java() {
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
            .withLogContaining(
                "@Injected test.TestMethodInjection.InnerClass.m; the parent class must not be private."
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenContainingClassIsPrivate_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            class TestMethodInjection {
              private class InnerClass {
                @Inject
                fun m(foo: Foo) {}
              }
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Injected test.TestMethodInjection.InnerClass.m; the parent class must not be private."
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenInjectedMethodParameterIsInvalidLazy_java() {
        @Suppress("rawtypes")
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
            .withLogContaining(
                "Type of test.TestMethodInjection.m is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenInjectedMethodParameterIsInvalidLazy_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestMethodInjection {
              @Inject
              fun m(foo: Lazy<*>) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Type of test.TestMethodInjection.m is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenInjectedMethodParameterIsInvalidProvider_java() {
        @Suppress("rawtypes")
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
            .withLogContaining(
                "Type of test.TestMethodInjection.m is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testMethodInjection_shouldFail_whenInjectedMethodParameterIsInvalidProvider_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Provider
            class TestMethodInjection {
              @Inject
              fun m(foo: Provider<*>) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Type of test.TestMethodInjection.m is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testOverrideMethodInjection_java() {
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

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testOverrideMethodInjection_expected)
    }

    @Test
    fun testOverrideMethodInjection_kt() {
        val source = ktSource(
            "TestMethodInjectionParent",
            """
            package test
            import javax.inject.Inject
            class TestMethodInjectionParent {
              @Inject
              fun m(foo: Foo) {}
              class TestMethodInjection : TestMethodInjectionParent() {
                @Inject
                fun m(foo: Foo) {}
              }
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testOverrideMethodInjection_expected)
    }

    @Suppress("RemoveRedundantBackticks")
    private val testOverrideMethodInjection_expected = expectedKtSource(
        "test/TestMethodInjectionParent\$TestMethodInjection__MemberInjector",
        """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier",
              "UNCHECKED_CAST"
            )
            public class `TestMethodInjectionParent${'$'}TestMethodInjection__MemberInjector` :
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

    @Test
    fun testMethodInjection_withException_java() {
        @Suppress("RedundantThrows")
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

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testMethodInjection_withException_expected)
    }

    @Test
    fun testMethodInjection_withException_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            class TestMethodInjection {
              @Inject
              @Throws(Exception::class)
              fun m(foo: Foo) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testMethodInjection_withException_expected)
    }

    private val testMethodInjection_withException_expected = expectedKtSource(
        "test/TestMethodInjection__MemberInjector",
        """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier",
              "UNCHECKED_CAST"
            )
            public class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1 = scope.getInstance(Foo::class.java) as Foo
                target.m(param1)
              }
            }
            """
    )

    @Test
    fun testMethodInjection_withExceptions_java() {
        @Suppress("RedundantThrows", "DuplicateThrows")
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

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testMethodInjection_withExceptions_expected)
    }

    @Test
    fun testMethodInjection_withExceptions_kt() {
        val source = ktSource(
            "TestMethodInjection",
            """
            package test
            import javax.inject.Inject
            class TestMethodInjection {
              @Inject
              @Throws(Exception::class, Throwable::class)
              fun m(foo: Foo) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testMethodInjection_withExceptions_expected)
    }

    private val testMethodInjection_withExceptions_expected = expectedKtSource(
        "test/TestMethodInjection__MemberInjector",
        """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress(
              "ClassName",
              "RedundantVisibilityModifier",
              "UNCHECKED_CAST"
            )
            public class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1 = scope.getInstance(Foo::class.java) as Foo
                target.m(param1)
              }
            }
            """
    )
}
