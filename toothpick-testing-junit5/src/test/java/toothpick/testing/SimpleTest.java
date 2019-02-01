package toothpick.testing;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SimpleTest {

  private static boolean wasRun = false;

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this) {
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
      super.afterEach(context);
      wasRun = true;
    }
  };

  @Test
  void test() {
    // THEN
    assertThat(wasRun, is(false));
  }

  @AfterAll
  static void tearDownAll() {
    // THEN
    assertThat(wasRun, is(true));
  }
}
