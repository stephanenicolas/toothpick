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
package toothpick.compiler.memberinjector;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static toothpick.compiler.memberinjector.ProcessorTestUtilities.memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;
import toothpick.compiler.factory.BaseFactoryTest;

public class RelaxedMemberInjectorWarningsTest extends BaseFactoryTest {
  @Test
  public void testInjectedMethod_shouldFailTheBuild_whenMethodIsPublic() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestWarningVisibleInjectedMethod",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import javax.inject.Singleton;", //
                    "public class TestWarningVisibleInjectedMethod {", //
                    "  @Inject public void init() {}", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible())
        .failsToCompile();
  }

  @Test
  public void testInjectedMethod_shouldNotFailTheBuild_whenMethodIsPublicButAnnotated() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestWarningVisibleInjectedMethod",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import javax.inject.Singleton;", //
                    "public class TestWarningVisibleInjectedMethod {", //
                    "  @SuppressWarnings(\"visible\")", //
                    "  @Inject public void init() {}", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible())
        .compilesWithoutError();
  }

  @Test
  public void testInjectedMethod_shouldFailTheBuild_whenMethodIsProtected() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestWarningVisibleInjectedMethod",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import javax.inject.Singleton;", //
                    "public class TestWarningVisibleInjectedMethod {", //
                    "  @Inject protected void init() {}", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible())
        .failsToCompile();
  }

  @Test
  public void testInjectedMethod_shouldNotFailTheBuild_whenMethodIsProtectedButAnnotated() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestWarningVisibleInjectedMethod",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "import javax.inject.Singleton;", //
                    "public class TestWarningVisibleInjectedMethod {", //
                    "  @SuppressWarnings(\"visible\")", //
                    "  @Inject protected void init() {}", //
                    "}"));

    assert_()
        .about(javaSource())
        .that(source)
        .processedWith(memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible())
        .compilesWithoutError();
  }
}
