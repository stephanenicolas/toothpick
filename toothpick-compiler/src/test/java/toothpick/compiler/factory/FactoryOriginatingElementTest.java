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
package toothpick.compiler.factory;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.annotation.processing.Processor;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.junit.Test;

public class FactoryOriginatingElementTest {

  @Test
  public void testOriginatingElement() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestOriginatingElement",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import javax.inject.Inject;", //
                    "public class TestOriginatingElement {", //
                    "  @Inject public TestOriginatingElement() {}", //
                    "}" //
                    ));

    Iterable<? extends Processor> processors = ProcessorTestUtilities.factoryProcessors();

    assert_().about(javaSource()).that(source).processedWith(processors).compilesWithoutError();

    FactoryProcessor factoryProcessor = (FactoryProcessor) processors.iterator().next();
    TypeElement enclosingElement =
        factoryProcessor.getOriginatingElement("test.TestOriginatingElement__Factory");
    assertTrue(enclosingElement.getQualifiedName().contentEquals("test.TestOriginatingElement"));
  }
}
