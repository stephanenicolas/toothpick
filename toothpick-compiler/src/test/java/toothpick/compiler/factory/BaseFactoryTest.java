package toothpick.compiler.factory;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class BaseFactoryTest {
  protected void assertThatCompileWithoutErrorButNoFactoryIsNotCreated(JavaFileObject source, String noFactoryPackageName, String noFactoryClass) {
    try {
      assert_().about(javaSource())
          .that(source)
          .processedWith(ProcessorTestUtilities.factoryAndMemberInjectorProcessors())
          .compilesWithoutError()
          .and()
          .generatesFileNamed(StandardLocation.locationFor("CLASS_OUTPUT"), "test", noFactoryClass + "$$Factory.class");
      fail("No optimistic factory should be created for an interface");
    } catch (AssertionError e) {
      assertThat(e.getMessage(), containsString(
          String.format("Did not find a generated file corresponding to %s$$Factory.class in package %s;", noFactoryClass, noFactoryPackageName)));
    }
  }

}
