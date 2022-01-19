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
package toothpick.compiler

import org.junit.Test
import toothpick.compiler.factory.FactoryProcessorProvider
import toothpick.compiler.memberinjector.MemberInjectorProcessorProvider

class FactoryAndMemberInjectorTests {

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

        val expectedSource = expectedKtSource(
            "test/TestAClassThatNeedsInjection__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestAClassThatNeedsInjection__Factory : Factory<TestAClassThatNeedsInjection> {
              public override fun createInstance(scope: Scope): TestAClassThatNeedsInjection {
                val testAClassThatNeedsInjection: TestAClassThatNeedsInjection = TestAClassThatNeedsInjection()
                return testAClassThatNeedsInjection
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
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
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

        val expectedSource = expectedKtSource(
            "test/TestAInnerClassThatNeedsInjection\$InnerClass__Factory",
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
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
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

        val expectedSource = expectedKtSource(
            "test/TestAClassThatNeedsInjection__Factory",
            """
            package test
            
            import kotlin.Boolean
            import kotlin.Suppress
            import toothpick.Factory
            import toothpick.MemberInjector
            import toothpick.Scope
            
            @Suppress("ClassName")
            internal class TestAClassThatNeedsInjection__Factory : Factory<TestAClassThatNeedsInjection> {
              private val memberInjector: MemberInjector<SuperClassThatNeedsInjection> =
                  SuperClassThatNeedsInjection__MemberInjector()
            
              public override fun createInstance(scope: Scope): TestAClassThatNeedsInjection {
                val scope = getTargetScope(scope)
                val testAClassThatNeedsInjection: TestAClassThatNeedsInjection = TestAClassThatNeedsInjection()
                memberInjector.inject(testAClassThatNeedsInjection, scope)
                return testAClassThatNeedsInjection
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
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
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

        val expectedSource = expectedKtSource(
            "test/TestAClassThatNeedsInjection__Factory",
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
            .processedWith(FactoryProcessorProvider(), MemberInjectorProcessorProvider())
            .compilesWithoutError()
            .generatesSources(expectedSource)
    }
}
