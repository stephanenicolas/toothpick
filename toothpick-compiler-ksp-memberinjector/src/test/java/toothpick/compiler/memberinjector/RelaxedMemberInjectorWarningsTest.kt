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
import toothpick.compiler.common.ToothpickOptions.Companion.CrashWhenInjectedMethodIsNotPackageVisible
import toothpick.compiler.compilationAssert
import toothpick.compiler.compilesWithoutError
import toothpick.compiler.failsToCompile
import toothpick.compiler.javaSource
import toothpick.compiler.ktSource
import toothpick.compiler.processedWith
import toothpick.compiler.that
import toothpick.compiler.withOptions

class RelaxedMemberInjectorWarningsTest {

    @Test
    fun testInjectedMethod_shouldFailTheBuild_whenMethodIsPublic_java() {
        val source = javaSource(
            "TestWarningVisibleInjectedMethod",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Singleton;
            public class TestWarningVisibleInjectedMethod {
              @Inject public void init() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .withOptions(CrashWhenInjectedMethodIsNotPackageVisible to "true")
            .failsToCompile()
    }

    @Test
    fun testInjectedMethod_shouldFailTheBuild_whenMethodIsPublic_kt() {
        val source = ktSource(
            "TestWarningVisibleInjectedMethod",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Singleton
            class TestWarningVisibleInjectedMethod {
              @Inject fun init()
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .withOptions(CrashWhenInjectedMethodIsNotPackageVisible to "true")
            .failsToCompile()
    }

    @Test
    fun testInjectedMethod_shouldNotFailTheBuild_whenMethodIsPublicButAnnotated_java() {
        val source = javaSource(
            "TestWarningVisibleInjectedMethod",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Singleton;
            public class TestWarningVisibleInjectedMethod {
              @SuppressWarnings("visible")
              @Inject public void init() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .withOptions(CrashWhenInjectedMethodIsNotPackageVisible to "true")
            .compilesWithoutError()
    }

    @Test
    fun testInjectedMethod_shouldNotFailTheBuild_whenMethodIsPublicButAnnotated_kt() {
        val source = ktSource(
            "TestWarningVisibleInjectedMethod",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Singleton
            class TestWarningVisibleInjectedMethod {
              @Suppress("visible")
              @Inject fun init() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .withOptions(CrashWhenInjectedMethodIsNotPackageVisible to "true")
            .compilesWithoutError()
    }

    @Test
    fun testInjectedMethod_shouldFailTheBuild_whenMethodIsProtected_java() {
        val source = javaSource(
            "TestWarningVisibleInjectedMethod",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Singleton;
            public class TestWarningVisibleInjectedMethod {
              @Inject protected void init() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .withOptions(CrashWhenInjectedMethodIsNotPackageVisible to "true")
            .failsToCompile()
    }

    @Test
    fun testInjectedMethod_shouldFailTheBuild_whenMethodIsProtected_kt() {
        @Suppress("ProtectedInFinal")
        val source = ktSource(
            "TestWarningVisibleInjectedMethod",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Singleton
            class TestWarningVisibleInjectedMethod {
              @Inject protected fun init() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .withOptions(CrashWhenInjectedMethodIsNotPackageVisible to "true")
            .failsToCompile()
    }

    @Test
    fun testInjectedMethod_shouldNotFailTheBuild_whenMethodIsProtectedButAnnotated_java() {
        val source = javaSource(
            "TestWarningVisibleInjectedMethod",
            """
            package test;
            import javax.inject.Inject;
            import javax.inject.Singleton;
            public class TestWarningVisibleInjectedMethod {
              @SuppressWarnings("visible")
              @Inject protected void init() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .withOptions(CrashWhenInjectedMethodIsNotPackageVisible to "true")
            .compilesWithoutError()
    }

    @Test
    fun testInjectedMethod_shouldNotFailTheBuild_whenMethodIsProtectedButAnnotated_kt() {
        @Suppress("ProtectedInFinal")
        val source = ktSource(
            "TestWarningVisibleInjectedMethod",
            """
            package test
            import javax.inject.Inject
            import javax.inject.Singleton
            class TestWarningVisibleInjectedMethod {
              @Suppress("visible")
              @Inject protected fun init() {}
            }
            """
        )

        compilationAssert()
            .that(source)
            .processedWith(MemberInjectorProcessorProvider())
            .withOptions(CrashWhenInjectedMethodIsNotPackageVisible to "true")
            .compilesWithoutError()
    }
}
