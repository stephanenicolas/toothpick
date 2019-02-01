package toothpick.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestScopeName {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "Foo");
  @RegisterExtension ToothPickExtension toothPickRuleWithoutScopeName = new ToothPickExtension(this);

  @Test
  void testSetScopeName_shouldFail_whenScopeNameWasAlreadySet() {
    assertThrows(IllegalStateException.class, new Executable() {
      @Override
      public void execute() {
        toothPickExtension.setScopeName("Bar");
      }
    });
  }

  @Test
  void testSetScopeName_shouldFail_whenScopeNameAlreadyContainsATestModule() {
    assertThrows(IllegalStateException.class, new Executable() {
      @Override
      public void execute() {
        toothPickRuleWithoutScopeName.setScopeName("Foo");
      }
    });
  }

  @Test
  void testScopeNameSetByConstruction() {
    assertThat(toothPickExtension.getScope(), notNullValue());
    assertThat(toothPickExtension.getScope().getName(), is((Object) "Foo"));
    assertThat(toothPickExtension.getTestModule(), notNullValue());
  }

  @Test
  void testSetScopeName() {
    toothPickRuleWithoutScopeName.setScopeName("Bar");
    assertThat(toothPickRuleWithoutScopeName.getScope(), notNullValue());
    assertThat(toothPickRuleWithoutScopeName.getScope().getName(), is((Object) "Bar"));
    assertThat(toothPickRuleWithoutScopeName.getTestModule(), notNullValue());
  }
}
