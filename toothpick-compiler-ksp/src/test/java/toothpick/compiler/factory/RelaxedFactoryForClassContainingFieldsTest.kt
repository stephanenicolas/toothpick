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

import org.junit.Test
import toothpick.compiler.*
import toothpick.compiler.factory.ProcessorTestUtilities.factoryAndMemberInjectorProcessors

class RelaxedFactoryForClassContainingFieldsTest {

    @Test
    fun testRelaxedFactoryCreationForInjectedField() {
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

        val expectedSource = expectedKtSource(
            "TestRelaxedFactoryCreationForInjectField__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestRelaxedFactoryCreationForInjectField__Factory :
                Factory<TestRelaxedFactoryCreationForInjectField> {
              private val memberInjector: MemberInjector<TestRelaxedFactoryCreationForInjectField> =
                  TestRelaxedFactoryCreationForInjectField__MemberInjector()
            
              public override fun createInstance(scope: Scope): TestRelaxedFactoryCreationForInjectField {
                val scope = getTargetScope(scope)
                val testRelaxedFactoryCreationForInjectField: TestRelaxedFactoryCreationForInjectField =
                    TestRelaxedFactoryCreationForInjectField()
                memberInjector.inject(testRelaxedFactoryCreationForInjectField, scope)
                return testRelaxedFactoryCreationForInjectField
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
            .processedWith(factoryAndMemberInjectorProcessors())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenFieldIsPrivate() {
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
            .processedWith(factoryAndMemberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "@Inject annotated fields must be non private : test.TestRelaxedFactoryCreationForInjectField#foo"
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenContainingClassIsPrivate() {
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
            .processedWith(factoryAndMemberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "@Injected fields in class InnerClass. The class must be non private."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenFieldIsInvalidLazy() {
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
            .processedWith(factoryAndMemberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Field test.TestRelaxedFactoryCreationForInjectField#foo is not a valid toothpick.Lazy."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldFail_WhenFieldIsInvalidProvider() {
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
            .processedWith(factoryAndMemberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Field test.TestRelaxedFactoryCreationForInjectField#foo is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testRelaxedFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeIsAbstract() {
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
    fun testRelaxedFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasANonDefaultConstructor() {
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
    fun testRelaxedFactoryCreationForInjectedField_shouldWorkButNoFactoryIsProduced_whenTypeHasAPrivateDefaultConstructor() {
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
}
