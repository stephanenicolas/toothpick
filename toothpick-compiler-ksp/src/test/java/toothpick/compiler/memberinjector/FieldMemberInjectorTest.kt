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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestFieldInjection__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "@Inject annotated fields must be non private : test.TestFieldInjection#foo"
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "@Injected fields in class InnerClass. The class must be non private."
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining("Field test.TestFieldInjection#foo is not a valid toothpick.Lazy.")
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Field test.TestFieldInjection#foo is not a valid javax.inject.Provider."
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Lazy/Provider foo is not a valid in TestFieldInjection. Lazy/Provider cannot be used on generic types."
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Lazy/Provider foo is not a valid in TestFieldInjection. Lazy/Provider cannot be used on generic types."
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
            "TestMemberInjection\$InnerClass__MemberInjector",
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
            .processedWith(memberInjectorProcessors())
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
            "TestMemberInjection__MemberInjector",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.MemberInjector;
            import toothpick.Scope;
            
            public final class TestMemberInjection__MemberInjector implements MemberInjector<TestMemberInjection> {
              private MemberInjector<TestMemberInjectionParent> superMemberInjector = new test.TestMemberInjectionParent__MemberInjector();
            
              @Override
              public void inject(TestMemberInjection target, Scope scope) {
                superMemberInjector.inject(target, scope);
                target.foo = scope.getInstance(Foo.class);
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
            "TestMemberInjection__MemberInjector",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.MemberInjector;
            import toothpick.Scope;
            
            public final class TestMemberInjection__MemberInjector implements MemberInjector<TestMemberInjection> {
              private MemberInjector<TestMemberInjectionParent> superMemberInjector = new test.TestMemberInjectionParent__MemberInjector();
            
              @Override
              public void inject(TestMemberInjection target, Scope scope) {
                superMemberInjector.inject(target, scope);
                target.foo = scope.getInstance(Foo.class);
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
            .processedWith(memberInjectorProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Field test.TestFieldInjection#foo is of type int which is not supported by Toothpick."
            )
    }
}
