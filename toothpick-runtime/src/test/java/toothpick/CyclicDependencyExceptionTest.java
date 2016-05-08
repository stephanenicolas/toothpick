package toothpick;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import toothpick.data.Bar;
import toothpick.data.BarChild;
import toothpick.data.CyclicFoo;
import toothpick.data.Foo;
import toothpick.data.FooProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class CyclicDependencyExceptionTest {

  @Test
  public void newCyclicDependencyException_showGenerateWholePath_whenCycleStartsPath() throws Exception {
    String expectedErrorMessage =
        "Class toothpick.data.CyclicFoo creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Bar      ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Foo      ||\n"
            + "              ||              ||\n"
            + "    toothpick.data.BarChild   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    //GIVEN
    List<Class> path = new ArrayList<>();
    path.add(CyclicFoo.class);
    path.add(Bar.class);
    path.add(Foo.class);
    path.add(BarChild.class);

    //WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, CyclicFoo.class);

    //THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test
  public void newCyclicDependencyException_showGeneratePartialPath_whenCycleStartsInTheMiddle() throws Exception {
    String expectedErrorMessage =
        "Class toothpick.data.CyclicFoo creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Foo      ||\n"
            + "              ||              ||\n"
            + "    toothpick.data.BarChild   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    //GIVEN
    List<Class> path = new ArrayList<>();
    path.add(Bar.class);
    path.add(CyclicFoo.class);
    path.add(Foo.class);
    path.add(BarChild.class);

    //WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, CyclicFoo.class);

    //THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test
  public void newCyclicDependencyException_showGenerateLastElementPath_whenCycleFinishesPath() throws Exception {
    String expectedErrorMessage =
        "Class toothpick.data.CyclicFoo creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    //GIVEN
    List<Class> path = new ArrayList<>();
    path.add(Bar.class);
    path.add(Foo.class);
    path.add(BarChild.class);
    path.add(CyclicFoo.class);

    //WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, CyclicFoo.class);

    //THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test
  public void newCyclicDependencyException_showGenerateWholePath_whenStartCannotBeFound() throws Exception {
    String expectedErrorMessage =
        "Class toothpick.data.FooProvider creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Bar      ||\n"
            + "              ||              ||\n"
            + "      toothpick.data.Foo      ||\n"
            + "              ||              ||\n"
            + "    toothpick.data.BarChild   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    //GIVEN
    List<Class> path = new ArrayList<>();
    path.add(CyclicFoo.class);
    path.add(Bar.class);
    path.add(Foo.class);
    path.add(BarChild.class);

    //WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, FooProvider.class);

    //THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test
  public void newCyclicDependencyException_showGenerateOneElementPath_whenCycleContainsOneElement() throws Exception {
    String expectedErrorMessage =
        "Class toothpick.data.CyclicFoo creates a cycle:\n"
            + "\n"
            + "               ================\n"
            + "              ||              ||\n"
            + "              \\/              ||\n"
            + "   toothpick.data.CyclicFoo   ||\n"
            + "              ||              ||\n"
            + "               ================\n";

    //GIVEN
    List<Class> path = new ArrayList<>();
    path.add(CyclicFoo.class);

    //WHEN
    CyclicDependencyException exception = new CyclicDependencyException(path, CyclicFoo.class);

    //THEN
    assertThat(exception.getMessage(), is(expectedErrorMessage));
  }

  @Test(expected = NullPointerException.class)
  public void newCyclicDependencyException_showThrowNullPointerException_whenPathIsNull() throws Exception {
    //GIVEN, WHEN
    new CyclicDependencyException(null, CyclicFoo.class);

    //THEN
    fail("Should throw an exception as a wrong parameters are passed");
  }

  @Test(expected = IllegalArgumentException.class)
  public void newCyclicDependencyException_showThrowIllegalArgumentException_whenPathIsEmpty() throws Exception {
    //GIVEN, WHEN
    new CyclicDependencyException(new ArrayList<Class>(), CyclicFoo.class);

    //THEN
    fail("Should throw an exception as a wrong parameters are passed");
  }

  @Test(expected = NullPointerException.class)
  public void newCyclicDependencyException_showThrowNullPointerException_whenStartClassIsNull() throws Exception {
    //GIVEN
    List<Class> path = new ArrayList<>();
    path.add(CyclicFoo.class);

    //WHEN
    new CyclicDependencyException(path, null);

    //THEN
    fail("Should throw an exception as a wrong parameters are passed");
  }
}

