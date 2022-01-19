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
import toothpick.compiler.assertLogs
import toothpick.compiler.compilationAssert
import toothpick.compiler.compilesWithoutError
import toothpick.compiler.expectedKtSource
import toothpick.compiler.failsToCompile
import toothpick.compiler.generatesSources
import toothpick.compiler.javaSource
import toothpick.compiler.processedWith
import toothpick.compiler.that

class FieldMemberInjectorTest {

    @Test
    fun testSimpleFieldInjection() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java)
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
    fun testNamedFieldInjection_whenUsingNamed() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java, "bar")
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
    fun testNamedFieldInjection_whenUsingQualifierAnnotation() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java, "test.Bar")
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
    fun testNamedFieldInjection_whenUsingNonQualifierAnnotation() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java)
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
    fun testNamedProviderFieldInjection_whenUsingQualifierAnnotation() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getProvider(Foo::class.java, "test.Bar")
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
    fun testNamedProviderFieldInjection_whenUsingNonQualifierAnnotation() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getProvider(Foo::class.java)
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
    fun testNamedFieldInjection_shouldWork_whenUsingMoreThan2Annotation_butOnly1Qualifier() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java, "test.Bar")
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
    fun testNamedFieldInjection_shouldFail_whenUsingMoreThan1QualifierAnnotations() {
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
            .assertLogs(
                "Only one javax.inject.Qualifier annotation is allowed to name injections."
            )
    }

    @Test
    fun testProviderFieldInjection() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getProvider(Foo::class.java)
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
    fun testLazyFieldInjection() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getLazy(Foo::class.java)
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
    fun testLazyFieldInjectionOfGenericTypeButNotDeclaringLazyOfGenericType() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getLazy(Foo::class.java)
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
    fun testFieldInjection_shouldProduceMemberInjector_whenClassHas2Fields() {
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

        val expectedSource = expectedKtSource(
            "test/TestFieldInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestFieldInjection__MemberInjector : MemberInjector<TestFieldInjection> {
              public override fun inject(target: TestFieldInjection, scope: Scope): Unit {
                target.foo = scope.getInstance(Foo::class.java)
                target.foo2 = scope.getInstance(Foo::class.java)
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
    fun testFieldInjection_shouldFail_whenFieldIsPrivate() {
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
            .assertLogs(
                "@Inject-annotated fields must not be private: test.TestFieldInjection.foo"
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenContainingClassIsPrivate() {
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
            .assertLogs(
                "@Injected test.TestFieldInjection.InnerClass.foo; the parent class must not be private."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidLazy() {
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
            .assertLogs("Type of test.TestFieldInjection.foo is not a valid toothpick.Lazy.")
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidProvider() {
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
            .assertLogs(
                "Type of test.TestFieldInjection.foo is not a valid javax.inject.Provider."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidLazyGenerics() {
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
            .assertLogs(
                "Lazy/Provider is not valid in test.TestFieldInjection.foo. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testFieldInjection_shouldFail_WhenFieldIsInvalidProviderGenerics() {
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
            .assertLogs(
                "Lazy/Provider is not valid in test.TestFieldInjection.foo. Lazy/Provider cannot be used on generic types."
            )
    }

    @Test
    fun testFieldInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassIsStaticHasInjectedMembers() {
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

        val expectedSource = expectedKtSource(
            "test/TestMemberInjection\$InnerClass__MemberInjector",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.MemberInjector;
            import toothpick.Scope;
            
            public final class TestMemberInjection${'$'}InnerClass__MemberInjector implements MemberInjector<TestMemberInjection.InnerClass> {
              private MemberInjector<TestMemberInjection.InnerSuperClass> superMemberInjector = new test.TestMemberInjection${'$'}InnerSuperClass__MemberInjector();
            
              @Override
              public void inject(TestMemberInjection.InnerClass target, Scope scope) {
                superMemberInjector.inject(target, scope);
                target.foo = scope.getInstance(Foo.class);
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
    fun testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembers() {
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

        val expectedSource = expectedKtSource(
            "test/TestMemberInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestMemberInjection__MemberInjector : MemberInjector<TestMemberInjection> {
              private val superMemberInjector: MemberInjector<TestMemberInjectionParent> =
                  TestMemberInjectionParent__MemberInjector()
            
              public override fun inject(target: TestMemberInjection, scope: Scope): Unit {
                superMemberInjector.inject(target, scope)
                target.foo = scope.getInstance(Foo::class.java)
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
    fun testMemberInjection_shouldInjectAsAnInstanceOfSuperClass_whenSuperClassHasInjectedMembersAndTypeArgument() {
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

        val expectedSource = expectedKtSource(
            "test/TestMemberInjection__MemberInjector",
            """
            package test
            
            import kotlin.Suppress
            import kotlin.Unit
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestMemberInjection__MemberInjector : MemberInjector<TestMemberInjection> {
              private val superMemberInjector: MemberInjector<TestMemberInjectionParent<*>> =
                  TestMemberInjectionParent__MemberInjector()
            
              public override fun inject(target: TestMemberInjection, scope: Scope): Unit {
                superMemberInjector.inject(target, scope)
                target.foo = scope.getInstance(Foo::class.java)
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
    @Ignore("KSP does not support checking if type is a Java primitive")
    fun testFieldInjection_shouldFail_WhenFieldIsPrimitive() {
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
            .assertLogs(
                "Field test.TestFieldInjection.foo is of type int which is not supported by Toothpick."
            )
    }
}
