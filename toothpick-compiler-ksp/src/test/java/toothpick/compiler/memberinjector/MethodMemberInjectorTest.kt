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
package toothpick.compiler.memberinjector

import org.junit.Test
import toothpick.compiler.*
import toothpick.compiler.memberinjector.ProcessorTestUtilities.memberInjectorProcessors

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
            "TestMethodInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestMethodInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestMethodInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestMethodInjection__MemberInjector",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Lazy;
            import toothpick.MemberInjector;
            import toothpick.Scope;
            
            public final class TestMethodInjection__MemberInjector implements MemberInjector<TestMethodInjection> {
              @Override
              public void inject(TestMethodInjection target, Scope scope) {
                Lazy<Foo> param1 = scope.getLazy(Foo.class);
                target.m(param1);
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(memberInjectorProcessors())
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Lazy/Provider foo is not a valid in m. Lazy/Provider cannot be used on generic types."
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "@Inject annotated methods must not be private : test.TestMethodInjection#m"
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "@Injected fields in class InnerClass. The class must be non private."
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Parameter foo in method/constructor test.TestMethodInjection#m is not a valid toothpick.Lazy."
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Parameter foo in method/constructor test.TestMethodInjection#m is not a valid javax.inject.Provider."
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
            "TestMethodInjectionParent\$TestMethodInjection__MemberInjector",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.MemberInjector;
            import toothpick.Scope;

            public class TestMethodInjectionParent${'$'}TestMethodInjection__MemberInjector implements MemberInjector<TestMethodInjectionParent.TestMethodInjection> {
              private MemberInjector<TestMethodInjectionParent> superMemberInjector = new test.TestMethodInjectionParent__MemberInjector();
            
              @Override
              public void inject(TestMethodInjectionParent.TestMethodInjection target, Scope scope) {
                superMemberInjector.inject(target, scope);
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(memberInjectorProcessors())
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
            "TestMethodInjection__MemberInjector",
            """
            package test
            
            import java.lang.Exception
            import java.lang.RuntimeException
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestMethodInjection__MemberInjector : MemberInjector<TestMethodInjection> {
              public override fun inject(target: TestMethodInjection, scope: Scope): Unit {
                val param1: Foo = scope.getInstance(Foo::class.java)
                try {
                  target.m(param1)
                } catch (e1: Exception) {
                  throw RuntimeException(e1)
                }
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(memberInjectorProcessors())
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
            "TestMethodInjection__MemberInjector",
            """
            package test;
            
            import java.lang.Exception;
            import java.lang.Override;
            import java.lang.RuntimeException;
            import java.lang.Throwable;
            import toothpick.MemberInjector;
            import toothpick.Scope;
            
            public final class TestMethodInjection__MemberInjector implements MemberInjector<TestMethodInjection> {
              @Override
              public void inject(TestMethodInjection target, Scope scope) {
                Foo param1 = scope.getInstance(Foo.class);
                try {
                  target.m(param1);
                } catch (Exception e1) {
                  throw new RuntimeException(e1);
                } catch (Throwable e2) {
                  throw new RuntimeException(e2);
                }
              }
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(memberInjectorProcessors())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }
}