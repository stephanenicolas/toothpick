package toothpick.compiler.factory;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class BaseFactoryTest {
  protected void assertThatCompileWithoutErrorButNoFactoryIsCreated(JavaFileObject source, String noFactoryPackageName, String noFactoryClass) {
    try {
      assert_().about(javaSource())
          .that(source)
          .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
          .compilesWithoutError()
          .and()
          .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", noFactoryClass + "$$Factory.class");
      fail("A factory was created when it shouldn't.");
    } catch (AssertionError e) {
      assertThat(e.getMessage(), containsString(
          String.format("generated the file named \"%s$$Factory.class\" in package \"%s\";", noFactoryClass, noFactoryPackageName)));
    }
  }
}
