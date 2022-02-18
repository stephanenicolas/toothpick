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
class RelaxedFactoryForClassContainingMethodsTest {

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectMethod {
              @Inject void m(Foo foo) {}
            }
            class Foo {}
           """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testRelaxedFactoryCreationForInjectedMethod_expected)
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectMethod {
              @Inject fun m(foo: Foo) {}
            }
            class Foo
           """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testRelaxedFactoryCreationForInjectedMethod_expected)
    }

    private val testRelaxedFactoryCreationForInjectedMethod_expected = expectedKtSource(
        "test/TestRelaxedFactoryCreationForInjectMethod__Factory",
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
            public class TestRelaxedFactoryCreationForInjectMethod__Factory :
                Factory<TestRelaxedFactoryCreationForInjectMethod> {
              private val memberInjector: MemberInjector<TestRelaxedFactoryCreationForInjectMethod> =
                  TestRelaxedFactoryCreationForInjectMethod__MemberInjector()
            
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestRelaxedFactoryCreationForInjectMethod {
                val scope = getTargetScope(scope)
                return TestRelaxedFactoryCreationForInjectMethod()
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
    fun testRelaxedFactoryCreationForInjectedMethod_shouldFail_WhenMethodIsPrivate_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectMethod {
              @Inject private void m(Foo foo) {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Inject-annotated methods must not be private: test.TestRelaxedFactoryCreationForInjectMethod.m"
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldFail_WhenMethodIsPrivate_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectMethod {
              @Inject private fun m(foo: Foo) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Inject-annotated methods must not be private: test.TestRelaxedFactoryCreationForInjectMethod.m"
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldFail_WhenContainingClassIsInvalid_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectMethod {
              private static class InnerClass {
                @Inject void m(Foo foo) {}
              }
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Injected test.TestRelaxedFactoryCreationForInjectMethod.InnerClass.m; the parent class must not be private."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldFail_WhenContainingClassIsInvalid_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectMethod {
              private class InnerClass {
                @Inject fun m(foo: Foo) {}
              }
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Injected test.TestRelaxedFactoryCreationForInjectMethod.InnerClass.m; the parent class must not be private."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldFail_WhenMethodParameterIsInvalidLazy_java() {
        @Suppress("rawtypes")
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestRelaxedFactoryCreationForInjectMethod {
              @Inject void m(Lazy foo) {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Type of test.TestRelaxedFactoryCreationForInjectMethod.m is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldFail_WhenMethodParameterIsInvalidLazy_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestRelaxedFactoryCreationForInjectMethod {
              @Inject fun m(foo: Lazy<*>) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Type of test.TestRelaxedFactoryCreationForInjectMethod.m is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldFail_WhenMethodParameterIsInvalidProvider_java() {
        @Suppress("rawtypes")
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestRelaxedFactoryCreationForInjectMethod {
              @Inject void m(Provider foo) {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Type of test.TestRelaxedFactoryCreationForInjectMethod.m is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldFail_WhenMethodParameterIsInvalidProvider_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Provider
            class TestRelaxedFactoryCreationForInjectMethod {
              @Inject fun m(foo: Provider<*>) {}
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Type of test.TestRelaxedFactoryCreationForInjectMethod.m is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeIsAbstract_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test;
            import javax.inject.Inject;
            public abstract class TestRelaxedFactoryCreationForInjectMethod {
              @Inject void m(Foo foo) {}
            }
            class Foo {}
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectMethod"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeIsAbstract_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test
            import javax.inject.Inject
            abstract class TestRelaxedFactoryCreationForInjectMethod {
              @Inject fun m(foo: Foo) {}
            }
            class Foo
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectMethod"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeHasANonDefaultConstructor_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectMethod {
              @Inject void m(Foo foo) {}
              public TestRelaxedFactoryCreationForInjectMethod(String s) {}
            }
            class Foo {}
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectMethod"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeHasANonDefaultConstructor_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectMethod(s: String) {
              @Inject fun m(foo: Foo) {}
            }
            class Foo
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectMethod"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeHasAPrivateDefaultConstructor_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectMethod {
              @Inject void m(Foo foo) {}
              private TestRelaxedFactoryCreationForInjectMethod() {}
            }
            class Foo {}
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectMethod"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedMethod_shouldWorkButNoFactoryIsProduced_whenTypeHasAPrivateDefaultConstructor_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectMethod",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectMethod private constructor() {
              @Inject fun m(foo: Foo) {}
            }
            class Foo
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectMethod"
        )
    }
}
