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

import org.junit.Ignore
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
class FieldMemberInjectorTest {

    @Test
    fun testSimpleFieldInjection_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestFieldInjection {
              @Inject Foo foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testSimpleFieldInjection_expected)
    }

    @Test
    fun testSimpleFieldInjection_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            class TestFieldInjection {
              @Inject lateinit var foo: Foo
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testSimpleFieldInjection_expected)
    }

    private val testSimpleFieldInjection_expected = expectedKtSource(
        "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java) as Foo
              }
            }
            """
    )

    @Test
    fun testNamedFieldInjection_whenUsingNamed_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Named;
            public class TestFieldInjection {
              @Inject @Named("bar") Foo foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedFieldInjection_whenUsingNamed_expected)
    }

    @Test
    fun testNamedFieldInjection_whenUsingNamed_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Named
            class TestFieldInjection {
              @Inject @Named("bar") lateinit var foo: Foo
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedFieldInjection_whenUsingNamed_expected)
    }

    private val testNamedFieldInjection_whenUsingNamed_expected = expectedKtSource(
        "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java, "bar") as Foo
              }
            }
            """
    )

    @Test
    fun testNamedFieldInjection_whenUsingQualifierAnnotation_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Named;
            import javax.inject.Qualifier;
            public class TestFieldInjection {
              @Inject @Bar Foo foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            @Qualifier
            @interface Bar {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedFieldInjection_whenUsingQualifierAnnotation_expected)
    }

    @Test
    fun testNamedFieldInjection_whenUsingQualifierAnnotation_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Named
            import javax.inject.Qualifier
            class TestFieldInjection {
              @Inject @Bar lateinit var foo: Foo
            }
            class Foo
            @Qualifier
            annotation class Bar
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedFieldInjection_whenUsingQualifierAnnotation_expected)
    }

    private val testNamedFieldInjection_whenUsingQualifierAnnotation_expected = expectedKtSource(
        "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java, "test.Bar") as Foo
              }
            }
            """
    )

    @Test
    fun testNamedFieldInjection_whenUsingNonQualifierAnnotation_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Named;
            public class TestFieldInjection {
              @Inject @Bar Foo foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            @interface Bar {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedFieldInjection_whenUsingNonQualifierAnnotation_expected)
    }

    @Test
    fun testNamedFieldInjection_whenUsingNonQualifierAnnotation_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Named
            class TestFieldInjection {
              @Inject @Bar lateinit var foo: Foo
            }
            class Foo
            annotation class Bar
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedFieldInjection_whenUsingNonQualifierAnnotation_expected)
    }

    private val testNamedFieldInjection_whenUsingNonQualifierAnnotation_expected = expectedKtSource(
        "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java) as Foo
              }
            }
            """
    )

    @Test
    fun testNamedProviderFieldInjection_whenUsingQualifierAnnotation_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Named;
            import javax.inject.Provider;
            import javax.inject.Qualifier;
            public class TestFieldInjection {
              @Inject @Bar Provider<Foo> foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            @Qualifier
            @interface Bar {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedProviderFieldInjection_whenUsingQualifierAnnotation_expected)
    }

    @Test
    fun testNamedProviderFieldInjection_whenUsingQualifierAnnotation_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Named
            import javax.inject.Provider
            import javax.inject.Qualifier
            class TestFieldInjection {
              @Inject @Bar lateinit var foo: Provider<Foo>
            }
            class Foo
            @Qualifier
            annotation class Bar
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedProviderFieldInjection_whenUsingQualifierAnnotation_expected)
    }

    private val testNamedProviderFieldInjection_whenUsingQualifierAnnotation_expected = expectedKtSource(
        "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getProvider(Foo::class.java, "test.Bar") as Provider<Foo>
              }
            }
            """
    )

    @Test
    fun testNamedProviderFieldInjection_whenUsingNonQualifierAnnotation_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Named;
            import javax.inject.Provider;
            public class TestFieldInjection {
              @Inject @Bar Provider<Foo> foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            @interface Bar {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedProviderFieldInjection_whenUsingNonQualifierAnnotation_expected)
    }

    @Test
    fun testNamedProviderFieldInjection_whenUsingNonQualifierAnnotation_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Named
            import javax.inject.Provider
            class TestFieldInjection {
              @Inject @Bar lateinit var foo: Provider<Foo>
            }
            class Foo
            annotation class Bar
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedProviderFieldInjection_whenUsingNonQualifierAnnotation_expected)
    }

    private val testNamedProviderFieldInjection_whenUsingNonQualifierAnnotation_expected = expectedKtSource(
        "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getProvider(Foo::class.java) as Provider<Foo>
              }
            }
            """
    )

    @Test
    fun testNamedFieldInjection_shouldWork_whenUsingMoreThan2Annotation_butOnly1Qualifier_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Named;
            import javax.inject.Qualifier;
            public class TestFieldInjection {
              @Inject @Bar @Qurtz Foo foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            @Qualifier
            @interface Bar {}
            @interface Qurtz {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedFieldInjection_shouldWork_whenUsingMoreThan2Annotation_butOnly1Qualifier_expected)
    }

    @Test
    fun testNamedFieldInjection_shouldWork_whenUsingMoreThan2Annotation_butOnly1Qualifier_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Named
            import javax.inject.Qualifier
            class TestFieldInjection {
              @Inject @Bar @Qurtz lateinit var foo: Foo
            }
            class Foo
            @Qualifier
            annotation class Bar
            annotation class Qurtz
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testNamedFieldInjection_shouldWork_whenUsingMoreThan2Annotation_butOnly1Qualifier_expected)
    }

    private val testNamedFieldInjection_shouldWork_whenUsingMoreThan2Annotation_butOnly1Qualifier_expected =
        expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java, "test.Bar") as Foo
              }
            }
            """
        )

    @Test
    fun testNamedFieldInjection_shouldFail_whenUsingMoreThan1QualifierAnnotations_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Named;
            import javax.inject.Qualifier;
            public class TestFieldInjection {
              @Inject @Bar @Qurtz Foo foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            @Qualifier
            @interface Bar {}
            @Qualifier
            @interface Qurtz {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Only one javax.inject.Qualifier annotation is allowed to name injections."
            )
    }

    @Test
    fun testNamedFieldInjection_shouldFail_whenUsingMoreThan1QualifierAnnotations_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Named
            import javax.inject.Qualifier
            class TestFieldInjection {
              @Inject @Bar @Qurtz lateinit var foo: Foo
            }
            class Foo
            @Qualifier
            annotation class Bar
            @Qualifier
            annotation class Qurtz
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Only one javax.inject.Qualifier annotation is allowed to name injections."
            )
    }

    @Test
    fun testProviderFieldInjection_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestFieldInjection {
              @Inject Provider<Foo> foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testProviderFieldInjection_expected)
    }

    @Test
    fun testProviderFieldInjection_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Provider
            class TestFieldInjection {
              @Inject lateinit var foo: Provider<Foo>
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testProviderFieldInjection_expected)
    }

    private val testProviderFieldInjection_expected = expectedKtSource(
        "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getProvider(Foo::class.java) as Provider<Foo>
              }
            }
            """
    )

    @Test
    fun testLazyFieldInjection_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestFieldInjection {
              @Inject Lazy<Foo> foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testLazyFieldInjection_expected)
    }

    @Test
    fun testLazyFieldInjection_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestFieldInjection {
              @Inject lateinit var foo: Lazy<Foo>
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testLazyFieldInjection_expected)
    }

    private val testLazyFieldInjection_expected = expectedKtSource(
        "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getLazy(Foo::class.java) as Lazy<Foo>
              }
            }
            """
    )

    @Test
    fun testLazyFieldInjectionOfGenericTypeButNotDeclaringLazyOfGenericType_java() {
        @Suppress("rawtypes")
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestFieldInjection {
              @Inject Lazy<Foo> foo;
              public TestFieldInjection() {}
            }
            class Foo<T> {}
            """
        )

        val expected = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getLazy(Foo::class.java) as Lazy<Foo<Any>>
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
    fun testLazyFieldInjectionOfGenericTypeButNotDeclaringLazyOfGenericType_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestFieldInjection {
              @Inject lateinit var foo: Lazy<Foo<*>>
            }
            class Foo<T>
            """
        )

        val expected = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getLazy(Foo::class.java) as Lazy<Foo<*>>
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
    fun testFieldInjection_shouldProduceMemberInjector_whenClassHas2Fields_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestFieldInjection {
              @Inject Foo foo;
              @Inject Foo foo2;
              public TestFieldInjection() {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testFieldInjection_shouldProduceMemberInjector_whenClassHas2Fields_expected)
    }

    @Test
    fun testFieldInjection_shouldProduceMemberInjector_whenClassHas2Fields_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            class TestFieldInjection {
              @Inject lateinit var foo: Foo
              @Inject lateinit var foo2: Foo
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(testFieldInjection_shouldProduceMemberInjector_whenClassHas2Fields_expected)
    }

    private val testFieldInjection_shouldProduceMemberInjector_whenClassHas2Fields_expected = expectedKtSource(
        "test/TestFieldInjection__MemberInjector",
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
            public class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java) as Foo
                target.foo2 = scope.getInstance(Foo::class.java) as Foo
              }
            }
            """
    )

    @Test
    fun testFieldInjection_shouldFail_whenFieldIsPrivate_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestFieldInjection {
              @Inject private Foo foo;
              public TestFieldInjection() {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Inject-annotated properties must not be private: test.TestFieldInjection.foo"
            )
    }

    @Test
    fun testFieldInjection_shouldFail_whenFieldIsPrivate_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            class TestFieldInjection {
              @Inject private val foo: Foo
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Inject-annotated properties must not be private: test.TestFieldInjection.foo"
            )
    }

    @Test
    fun testFieldInjection_shouldFail_whenFieldIsImmutable_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestFieldInjection {
              @Inject final Foo foo = null;
              public TestFieldInjection() {}
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Inject-annotated properties must be mutable: test.TestFieldInjection.foo"
            )
    }

    @Test
    fun testFieldInjection_shouldFail_whenFieldIsImmutable_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            class TestFieldInjection {
              @Inject val foo: Foo? = null
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "@Inject-annotated properties must be mutable: test.TestFieldInjection.foo"
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenContainingClassIsPrivate_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestFieldInjection {
              private static class InnerClass {
                @Inject Foo foo;
                public InnerClass() {}
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
                "@Injected test.TestFieldInjection.InnerClass.foo; the parent class must not be private."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenContainingClassIsPrivate_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            class TestFieldInjection {
              private class InnerClass {
                @Inject lateinit var foo: Foo
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
                "@Injected test.TestFieldInjection.InnerClass.foo; the parent class must not be private."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidLazy_java() {
        @Suppress("rawtypes")
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestFieldInjection {
              @Inject Lazy foo;
              public TestFieldInjection() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining("Type of test.TestFieldInjection.foo is not a valid toothpick.Lazy.")
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidLazy_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestFieldInjection {
              @Inject lateinit var foo: Lazy<*>
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining("Type of test.TestFieldInjection.foo is not a valid toothpick.Lazy.")
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidProvider_java() {
        @Suppress("rawtypes")
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestFieldInjection {
              @Inject Provider foo;
              public TestFieldInjection() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Type of test.TestFieldInjection.foo is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidProvider_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Provider
            class TestFieldInjection {
              @Inject lateinit var foo: Provider<*>
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Type of test.TestFieldInjection.foo is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidLazyGenerics_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import toothpick.Lazy;
            public class TestFieldInjection {
              @Inject Lazy<Foo<String>> foo;
              public TestFieldInjection() {}
            }
            class Foo<T> {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "test.TestFieldInjection.foo is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidLazyGenerics_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import toothpick.Lazy
            class TestFieldInjection {
              @Inject lateinit var foo: Lazy<Foo<String>>
            }
            class Foo<T>
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "test.TestFieldInjection.foo is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidProviderGenerics_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Provider;
            public class TestFieldInjection {
              @Inject Provider<Foo<String>> foo;
              public TestFieldInjection() {}
            }
            class Foo<T> {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "test.TestFieldInjection.foo is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidProviderGenerics_kt() {
        val source = ktSource(
            "TestFieldInjection",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Provider
            class TestFieldInjection {
              @Inject lateinit var foo: Provider<Foo<String>>
            }
            class Foo<T>
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "test.TestFieldInjection.foo is not a valid Lazy/Provider. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    @Ignore("https://github.com/tschuchortdev/kotlin-compile-testing/issues/105")
    fun testFieldInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassIsStaticHasInjectedMembers_java() {
        val source = javaSource(
            "TestMemberInjection",
            """
            package test;
            import javax.inject.Inject;
            class TestMemberInjection {
              public static class InnerSuperClass {
                @Inject Foo foo;
                public InnerSuperClass() {}
              }
              public static class InnerClass extends InnerSuperClass {
                @Inject Foo foo;
                public InnerClass() {}
              }
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testFieldInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassIsStaticHasInjectedMembers_expected
            )
    }

    @Test
    fun testFieldInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassIsStaticHasInjectedMembers_kt() {
        val source = ktSource(
            "TestMemberInjection",
            """
            package test
            import javax.inject.Inject
            class TestMemberInjection {
              class InnerSuperClass {
                @Inject lateinit var foo: Foo
              }
              class InnerClass : InnerSuperClass() {
                @Inject lateinit var foo: Foo
              }
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testFieldInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassIsStaticHasInjectedMembers_expected
            )
    }

    @Suppress("RemoveRedundantBackticks")
    private val testFieldInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassIsStaticHasInjectedMembers_expected =
        expectedKtSource(
            "test/TestMemberInjection\$InnerClass__MemberInjector",
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
            public class `TestMemberInjection${'$'}InnerClass__MemberInjector` :
                MemberInjector<TestMemberInjection.InnerClass> {
              private val superMemberInjector: MemberInjector<TestMemberInjection.InnerSuperClass> =
                  `TestMemberInjection${'$'}InnerSuperClass__MemberInjector`()
            
              public override fun inject(target: TestMemberInjection.InnerClass, scope: Scope): Unit {
                superMemberInjector.inject(target, scope)
                target.foo = scope.getInstance(Foo::class.java) as Foo
              }
            }
            """
        )

    @Test
    fun testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembers_java() {
        val source = javaSource(
            "TestMemberInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestMemberInjection extends TestMemberInjectionParent {
              @Inject Foo foo;
            }
            class TestMemberInjectionParent {
              @Inject Foo foo;
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembers_expected
            )
    }

    @Test
    fun testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembers_kt() {
        val source = ktSource(
            "TestMemberInjection",
            """
            package test
            import javax.inject.Inject
            class TestMemberInjection : TestMemberInjectionParent() {
              @Inject lateinit var foo: Foo
            }
            class TestMemberInjectionParent {
              @Inject lateinit var foo: Foo
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembers_expected
            )
    }

    private val testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembers_expected =
        expectedKtSource(
            "test/TestMemberInjection__MemberInjector",
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
            public class TestMemberInjection__MemberInjector : MemberInjector<TestMemberInjection> {
              private val superMemberInjector: MemberInjector<TestMemberInjectionParent> =
                  TestMemberInjectionParent__MemberInjector()
            
              public override fun inject(target: TestMemberInjection, scope: Scope): Unit {
                superMemberInjector.inject(target, scope)
                target.foo = scope.getInstance(Foo::class.java) as Foo
              }
            }
            """
        )

    @Test
    fun testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembersAndTypeArgument_java() {
        val source = javaSource(
            "TestMemberInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestMemberInjection extends TestMemberInjectionParent<Integer> {
              @Inject Foo foo;
            }
            class TestMemberInjectionParent<T> {
              @Inject Foo foo;
            }
            class Foo {}
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembersAndTypeArgument_expected
            )
    }

    @Test
    fun testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembersAndTypeArgument_kt() {
        val source = ktSource(
            "TestMemberInjection",
            """
            package test
            import javax.inject.Inject
            class TestMemberInjection : TestMemberInjectionParent<Integer>() {
              @Inject lateinit var foo: Foo
            }
            class TestMemberInjectionParent<T> {
              @Inject lateinit var foo: Foo
            }
            class Foo
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(
                testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembersAndTypeArgument_expected
            )
    }

    private val testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembersAndTypeArgument_expected =
        expectedKtSource(
            "test/TestMemberInjection__MemberInjector",
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
            public class TestMemberInjection__MemberInjector : MemberInjector<TestMemberInjection> {
              private val superMemberInjector: MemberInjector<TestMemberInjectionParent<*>> =
                  TestMemberInjectionParent__MemberInjector()
            
              public override fun inject(target: TestMemberInjection, scope: Scope): Unit {
                superMemberInjector.inject(target, scope)
                target.foo = scope.getInstance(Foo::class.java) as Foo
              }
            }
            """
        )

    @Test
    @Ignore("KSP does not support checking if type is a Java primitive")
    fun testFieldInjection_shouldFail_WhenFieldIsPrimitive_java() {
        val source = javaSource(
            "TestFieldInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestFieldInjection {
              @Inject int foo;
              public TestFieldInjection() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .failsToCompile()
            .withLogContaining(
                "Field test.TestFieldInjection.foo is of type int which is not supported by Toothpick."
            )
    }
}
