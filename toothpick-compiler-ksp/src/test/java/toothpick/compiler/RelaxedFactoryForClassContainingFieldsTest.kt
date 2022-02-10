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
class RelaxedFactoryForClassContainingFieldsTest {

    @Test
    fun testRelaxedFactoryCreationForInjectedField_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectField {
              @Inject Foo foo;
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testRelaxedFactoryCreationForInjectedField_expected)
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectField {
              @Inject val foo: Foo
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testRelaxedFactoryCreationForInjectedField_expected)
    }

    private val testRelaxedFactoryCreationForInjectedField_expected = expectedKtSource(
        "test/TestRelaxedFactoryCreationForInjectField__Factory",
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
            public class TestRelaxedFactoryCreationForInjectField__Factory :
                Factory<TestRelaxedFactoryCreationForInjectField> {
              private val memberInjector: MemberInjector<TestRelaxedFactoryCreationForInjectField> =
                  TestRelaxedFactoryCreationForInjectField__MemberInjector()
            
              @Suppress("NAME_SHADOWING")
              public override fun createInstance(scope: Scope): TestRelaxedFactoryCreationForInjectField {
                val scope = getTargetScope(scope)
                return TestRelaxedFactoryCreationForInjectField()
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
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenFieldIsPrivate_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectField {
              @Inject private Foo foo;
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "@Inject-annotated fields must not be private: test.TestRelaxedFactoryCreationForInjectField.foo"
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenFieldIsPrivate_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectField {
              @Inject private val foo: Foo
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "@Inject-annotated fields must not be private: test.TestRelaxedFactoryCreationForInjectField.foo"
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenContainingClassIsPrivate_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectField {
              private static class InnerClass {
                @Inject Foo foo;
              }
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "@Injected test.TestRelaxedFactoryCreationForInjectField.InnerClass.foo; the parent class must not be private"
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenContainingClassIsPrivate_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectField {
              private class InnerClass {
                @Inject val foo: Foo
              }
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "@Injected test.TestRelaxedFactoryCreationForInjectField.InnerClass.foo; the parent class must not be private"
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenFieldIsInvalidLazy_java() {
        @Suppress("rawtypes")
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestRelaxedFactoryCreationForInjectField {
              @Inject Lazy foo;
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of test.TestRelaxedFactoryCreationForInjectField.foo is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenFieldIsInvalidLazy_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestRelaxedFactoryCreationForInjectField {
              @Inject val foo: Lazy<*>
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of test.TestRelaxedFactoryCreationForInjectField.foo is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenFieldIsInvalidProvider_java() {
        @Suppress("rawtypes")
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestRelaxedFactoryCreationForInjectField {
              @Inject Provider foo;
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of test.TestRelaxedFactoryCreationForInjectField.foo is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenFieldIsInvalidProvider_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Provider
            class TestRelaxedFactoryCreationForInjectField {
              @Inject val foo: Provider<*>
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .failsToCompile()
            .assertLogs(
                "Type of test.TestRelaxedFactoryCreationForInjectField.foo is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeIsAbstract_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test;
            import javax.inject.Inject;
            public abstract class TestRelaxedFactoryCreationForInjectField {
              @Inject Foo foo;
            }
            class Foo {}
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectField"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeIsAbstract_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test
            import javax.inject.Inject
            abstract class TestRelaxedFactoryCreationForInjectField {
              @Inject val foo: Foo
            }
            class Foo
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectField"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasANonDefaultConstructor_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectField {
              @Inject Foo foo;
              public TestRelaxedFactoryCreationForInjectField(String s) {}
            }
            class Foo {}
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectField"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasANonDefaultConstructor_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectField(s: String) {
              @Inject val foo: Foo
            }
            class Foo
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectField"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasAPrivateDefaultConstructor_java() {
        val source = javaSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test;
            import javax.inject.Inject;
            public class TestRelaxedFactoryCreationForInjectField {
              @Inject Foo foo;
              private TestRelaxedFactoryCreationForInjectField() {}
            }
            class Foo {}
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectField"
        )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasAPrivateDefaultConstructor_kt() {
        val source = ktSource(
            "TestRelaxedFactoryCreationForInjectField",
            """
            package test
            import javax.inject.Inject
            class TestRelaxedFactoryCreationForInjectField private constructor() {
              @Inject val foo: Foo
            }
            class Foo
            """
        )

        assertThatCompileWithoutErrorButNoFactoryIsCreated(
            source = source,
            noFactoryClass = "TestRelaxedFactoryCreationForInjectField"
        )
    }
}
