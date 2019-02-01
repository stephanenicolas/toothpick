package toothpick.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

class TestInjectionAndGetInstance {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "Foo");

  private EntryPoint entryPoint;

  @Test
  void testGetInstance() {
    //GIVEN
    //WHEN
    entryPoint = toothPickExtension.getInstance(EntryPoint.class);

    //THEN
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint.dependency, notNullValue());
  }

  @Test
  void testInject() {
    //GIVEN
    EntryPoint entryPoint = new EntryPoint();

    //WHEN
    toothPickExtension.inject(entryPoint);

    //THEN
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint.dependency, notNullValue());
  }

  static class EntryPoint {
    @Inject Dependency dependency;
  }

  static class Dependency {
    @Inject Dependency() {
    }
  }
}
