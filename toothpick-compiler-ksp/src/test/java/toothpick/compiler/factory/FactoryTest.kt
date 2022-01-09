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
import toothpick.compiler.factory.ProcessorTestUtilities.factoryProcessors
import toothpick.compiler.factory.ProcessorTestUtilities.factoryProcessorsWithAdditionalTypes

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

        val expectedSource = expectedJavaSource(
            "TestEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestEmptyConstructor__Factory implements Factory<TestEmptyConstructor> {
              @Override
              public TestEmptyConstructor createInstance(Scope scope) {
                TestEmptyConstructor testEmptyConstructor = new TestEmptyConstructor();
                return testEmptyConstructor;
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestEmptyConstructor__Factory",
            """
            package test;

            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestEmptyConstructor__Factory implements Factory<TestEmptyConstructor> {
              @Override
              public TestEmptyConstructor createInstance(Scope scope) {
                TestEmptyConstructor testEmptyConstructor = new TestEmptyConstructor();
                return testEmptyConstructor;
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestEmptyConstructor__Factory implements Factory<TestEmptyConstructor> {
              @Override
              public TestEmptyConstructor createInstance(Scope scope) {
                TestEmptyConstructor testEmptyConstructor = new TestEmptyConstructor();
                return testEmptyConstructor;
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
            .processedWith(factoryProcessors())
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
            .processedWith(factoryProcessors())
            .failsToCompile()
            .withErrorContaining(
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
            .processedWith(factoryProcessors())
            .failsToCompile()
            .withErrorContaining(
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

        val expectedSource = expectedJavaSource(
            "Wrapper\$TestConstructorInProtectedClass__Factory",
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestConstructorInPackageClass__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestConstructorInPackageClass__Factory implements Factory<TestConstructorInPackageClass> {
              @Override
              public TestConstructorInPackageClass createInstance(Scope scope) {
                TestConstructorInPackageClass testConstructorInPackageClass = new TestConstructorInPackageClass();
                return testConstructorInPackageClass;
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
            .processedWith(factoryProcessors())
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
            .processedWith(factoryProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Class test.TestPrivateConstructor cannot have more than one @Inject annotated constructor."
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

        val expectedSource = expectedJavaSource(
            "Test2Constructors__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class Test2Constructors__Factory implements Factory<Test2Constructors> {
              @Override
              public Test2Constructors createInstance(Scope scope) {
                Test2Constructors test2Constructors = new Test2Constructors();
                return test2Constructors;
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
            .processedWith(factoryProcessors())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField() {
        val source = javaSource(
            "TestAClassThatNeedsInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestAClassThatNeedsInjection {
            @Inject String s;
              @Inject public TestAClassThatNeedsInjection() {}
            }
            """
        )

        val expectedSource = expectedJavaSource(
            "TestAClassThatNeedsInjection__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.MemberInjector;
            import toothpick.Scope;
            
            public final class TestAClassThatNeedsInjection__Factory implements Factory<TestAClassThatNeedsInjection> {
              private MemberInjector<TestAClassThatNeedsInjection> memberInjector = new test.TestAClassThatNeedsInjection__MemberInjector();
            
              @Override
              public TestAClassThatNeedsInjection createInstance(Scope scope) {
                scope = getTargetScope(scope);
                TestAClassThatNeedsInjection testAClassThatNeedsInjection = new TestAClassThatNeedsInjection();
                memberInjector.inject(testAClassThatNeedsInjection, scope);
                return testAClassThatNeedsInjection;
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
            .processedWith(factoryAndMemberInjectorProcessors())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testAInnerClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedField() {
        val source = javaSource(
            "TestAInnerClassThatNeedsInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestAInnerClassThatNeedsInjection {
              public static class InnerClass  {
                @Inject String s;
                public InnerClass() {}
              }
            }
            """
        )

        val expectedSource = expectedJavaSource(
            "TestAInnerClassThatNeedsInjection\$InnerClass__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.MemberInjector;
            import toothpick.Scope;
            
            public final class TestAInnerClassThatNeedsInjection${'$'}InnerClass__Factory implements Factory<TestAInnerClassThatNeedsInjection.InnerClass> {
              private MemberInjector<TestAInnerClassThatNeedsInjection.InnerClass> memberInjector = new test.TestAInnerClassThatNeedsInjection${'$'}InnerClass__MemberInjector();
            
              @Override
              public TestAInnerClassThatNeedsInjection.InnerClass createInstance(Scope scope) {
                scope = getTargetScope(scope);
                TestAInnerClassThatNeedsInjection.InnerClass innerClass = new TestAInnerClassThatNeedsInjection.InnerClass();
                memberInjector.inject(innerClass, scope);
                return innerClass;
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
            .processedWith(factoryAndMemberInjectorProcessors())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testAClassThatInheritFromAnotherClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnAnnotatedConstructor_andShouldUseSuperMemberInjector() {
        val source = javaSource(
            "TestAClassThatNeedsInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestAClassThatNeedsInjection extends SuperClassThatNeedsInjection {
              @Inject public TestAClassThatNeedsInjection() {}
            }
            class SuperClassThatNeedsInjection {
              @Inject String s;
              public SuperClassThatNeedsInjection() {}
            }
            """
        )

        val expectedSource = expectedJavaSource(
            "TestAClassThatNeedsInjection__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.MemberInjector;
            import toothpick.Scope;
            
            public final class TestAClassThatNeedsInjection__Factory implements Factory<TestAClassThatNeedsInjection> {
              private MemberInjector<SuperClassThatNeedsInjection> memberInjector = new test.SuperClassThatNeedsInjection__MemberInjector();
            
              @Override
              public TestAClassThatNeedsInjection createInstance(Scope scope) {
                scope = getTargetScope(scope);
                TestAClassThatNeedsInjection testAClassThatNeedsInjection = new TestAClassThatNeedsInjection();
                memberInjector.inject(testAClassThatNeedsInjection, scope);
                return testAClassThatNeedsInjection;
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
            .processedWith(factoryAndMemberInjectorProcessors())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
    fun testAClassThatNeedsInjection_shouldHaveAFactoryThatInjectsIt_whenItHasAnInjectedMethod() {
        val source = javaSource(
            "TestAClassThatNeedsInjection",
            """
            package test;
            import javax.inject.Inject;
            public class TestAClassThatNeedsInjection {
              @Inject public TestAClassThatNeedsInjection() {}
              @Inject public void m(String s) {}
            }
            """
        )

        val expectedSource = expectedJavaSource(
            "TestAClassThatNeedsInjection__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.MemberInjector;
            import toothpick.Scope;
            
            public final class TestAClassThatNeedsInjection__Factory implements Factory<TestAClassThatNeedsInjection> {
              private MemberInjector<TestAClassThatNeedsInjection> memberInjector = new test.TestAClassThatNeedsInjection__MemberInjector();
            
              @Override
              public TestAClassThatNeedsInjection createInstance(Scope scope) {
                scope = getTargetScope(scope);
                TestAClassThatNeedsInjection testAClassThatNeedsInjection = new TestAClassThatNeedsInjection();
                memberInjector.inject(testAClassThatNeedsInjection, scope);
                return testAClassThatNeedsInjection;
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
            .processedWith(factoryAndMemberInjectorProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestNonEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Integer;
            import java.lang.Override;
            import java.lang.String;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {
              @Override
              public TestNonEmptyConstructor createInstance(Scope scope) {
                scope = getTargetScope(scope);
                String param1 = scope.getInstance(String.class);
                Integer param2 = scope.getInstance(Integer.class);
                TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);
                return testNonEmptyConstructor;
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestNonEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Integer;
            import java.lang.Override;
            import java.lang.String;
            import toothpick.Factory;
            import toothpick.Lazy;
            import toothpick.Scope;
            
            public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {
              @Override
              public TestNonEmptyConstructor createInstance(Scope scope) {
                scope = getTargetScope(scope);
                Lazy<String> param1 = scope.getLazy(String.class);
                Integer param2 = scope.getInstance(Integer.class);
                TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);
                return testNonEmptyConstructor;
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestNonEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Integer;
            import java.lang.Override;
            import java.lang.String;
            import javax.inject.Provider;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {
              @Override
              public TestNonEmptyConstructor createInstance(Scope scope) {
                scope = getTargetScope(scope);
                Provider<String> param1 = scope.getProvider(String.class);
                Integer param2 = scope.getInstance(Integer.class);
                TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);
                return testNonEmptyConstructor;
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
            .processedWith(factoryProcessors())
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
            .processedWith(factoryProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Parameter lazy in method/constructor test.TestNonEmptyConstructor#<init> is not a valid toothpick.Lazy."
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
            .processedWith(factoryProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Parameter provider in method/constructor test.TestNonEmptyConstructor#<init> is not a valid javax.inject.Provider."
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

        val expectedSource = expectedJavaSource(
            "TestNonEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Override;
            import java.util.List;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {
              @Override
              public TestNonEmptyConstructor createInstance(Scope scope) {
                scope = getTargetScope(scope);
                List param1 = scope.getInstance(List.class);
                TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1);
                return testNonEmptyConstructor;
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
            .processedWith(factoryProcessors())
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
            .processedWith(factoryProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Lazy/Provider str is not a valid in <init>. Lazy/Provider cannot be used on generic types."
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
            .processedWith(factoryProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Lazy/Provider str is not a valid in <init>. Lazy/Provider cannot be used on generic types."
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
            .processedWith(factoryProcessors())
            .failsToCompile()
            .withErrorContaining(
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

        val expectedSource = expectedJavaSource(
            "TestClassConstructorThrowingException__Factory",
            """
            package test;
            
            import java.lang.Override;
            import java.lang.String;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestClassConstructorThrowingException__Factory implements Factory<TestClassConstructorThrowingException> {
              @Override
              public TestClassConstructorThrowingException createInstance(Scope scope) {
                scope = getTargetScope(scope);
                try {
                  String param1 = scope.getInstance(String.class);
                  TestClassConstructorThrowingException testClassConstructorThrowingException = new TestClassConstructorThrowingException(param1);
                  return testClassConstructorThrowingException;
                } catch(java.lang.Throwable ex) {
                  throw new java.lang.RuntimeException(ex);
                }
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestNonEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Integer;
            import java.lang.Override;
            import java.lang.String;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {
              @Override
              public TestNonEmptyConstructor createInstance(Scope scope) {
                scope = getTargetScope(scope);
                String param1 = scope.getInstance(String.class);
                Integer param2 = scope.getInstance(Integer.class);
                TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);
                return testNonEmptyConstructor;
              }
            
              @Override
              public Scope getTargetScope(Scope scope) {
                return scope.getRootScope();
              }
            
              @Override
              public boolean hasScopeAnnotation() {
                return true;
              }
            
              @Override
              public boolean hasSingletonAnnotation() {
                return true;
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestEmptyConstructor__Factory implements Factory<TestEmptyConstructor> {
              @Override
              public TestEmptyConstructor createInstance(Scope scope) {
                TestEmptyConstructor testEmptyConstructor = new TestEmptyConstructor();
                return testEmptyConstructor;
              }
            
              @Override
              public Scope getTargetScope(Scope scope) {
                return scope.getRootScope();
              }
            
              @Override
              public boolean hasScopeAnnotation() {
                return true;
              }
            
              @Override
              public boolean hasSingletonAnnotation() {
                return true;
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestNonEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Integer;
            import java.lang.Override;
            import java.lang.String;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {
              @Override
              public TestNonEmptyConstructor createInstance(Scope scope) {
                scope = getTargetScope(scope);
                String param1 = scope.getInstance(String.class);
                Integer param2 = scope.getInstance(Integer.class);
                TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);
                return testNonEmptyConstructor;
              }
            
              @Override
              public Scope getTargetScope(Scope scope) {
                return scope.getParentScope(test.CustomScope.class);
              }
            
              @Override
              public boolean hasScopeAnnotation() {
                return true;
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Override;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestEmptyConstructor__Factory implements Factory<TestEmptyConstructor> {
              @Override
              public TestEmptyConstructor createInstance(Scope scope) {
                TestEmptyConstructor testEmptyConstructor = new TestEmptyConstructor();
                return testEmptyConstructor;
              }
            
              @Override
              public Scope getTargetScope(Scope scope) {
                return scope.getParentScope(test.CustomScope.class);
              }
            
              @Override
              public boolean hasScopeAnnotation() {
                return true;
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
            .processedWith(factoryProcessorsWithAdditionalTypes("test.CustomScope"))
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

        val expectedSource = expectedJavaSource(
            "TestNonEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Integer;
            import java.lang.Override;
            import java.lang.String;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {
              @Override
              public TestNonEmptyConstructor createInstance(Scope scope) {
                scope = getTargetScope(scope);
                String param1 = scope.getInstance(String.class);
                Integer param2 = scope.getInstance(Integer.class);
                TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);
                return testNonEmptyConstructor;
              }
            
              @Override
              public Scope getTargetScope(Scope scope) {
                return scope.getParentScope(test.CustomScope.class);
              }
            
              @Override
              public boolean hasScopeAnnotation() {
                return true;
              }
            
              @Override
              public boolean hasSingletonAnnotation() {
                return true;
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
            .processedWith(factoryProcessors())
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

        val expectedSource = expectedJavaSource(
            "TestNonEmptyConstructor__Factory",
            """
            package test;
            
            import java.lang.Integer;
            import java.lang.Override;
            import java.lang.String;
            import toothpick.Factory;
            import toothpick.Scope;
            
            public final class TestNonEmptyConstructor__Factory implements Factory<TestNonEmptyConstructor> {
              @Override
              public TestNonEmptyConstructor createInstance(Scope scope) {
                scope = getTargetScope(scope);
                String param1 = scope.getInstance(String.class);
                Integer param2 = scope.getInstance(Integer.class);
                TestNonEmptyConstructor testNonEmptyConstructor = new TestNonEmptyConstructor(param1, param2);
                return testNonEmptyConstructor;
              }
            
              @Override
              public Scope getTargetScope(Scope scope) {
                return scope.getRootScope();
              }
            
              @Override
              public boolean hasScopeAnnotation() {
                return true;
              }
            
              @Override
              public boolean hasSingletonAnnotation() {
                return true;
              }
            
              @Override
              public boolean hasReleasableAnnotation() {
                return false;
              }
            
              @Override
              public boolean hasProvidesSingletonAnnotation() {
                return true;
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
            .processedWith(factoryProcessors())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }

    @Test
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
            .processedWith(factoryProcessors())
            .failsToCompile()
            .withErrorContaining(
                "Parameter n in method/constructor test.TestPrimitiveConstructor#<init> is of type int which is not supported by Toothpick."
            )
    }
}
