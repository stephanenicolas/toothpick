package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.annotation.processing.Processor;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.junit.Assert.assertTrue;

public class FactoryOriginatingElementTest {

  @Test
  public void testOriginatingElement() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestOriginatingElement", Joiner.on('\n').join(//
            "package test;", //
            "import javax.inject.Inject;", //
            "public class TestOriginatingElement {", //
            "  @Inject public TestOriginatingElement() {}", //
            "}" //
    ));

    Iterable<? extends Processor> processors = ProcessorTestUtilities.factoryProcessors();

    assert_().about(javaSource())
            .that(source)
            .processedWith(processors)
            .compilesWithoutError();

    FactoryProcessor factoryProcessor = (FactoryProcessor) processors.iterator().next();
    TypeElement enclosingElement = factoryProcessor.getOriginatingElement("test.TestOriginatingElement__Factory");
    assertTrue(enclosingElement.getQualifiedName().contentEquals("test.TestOriginatingElement"));
  }
}
