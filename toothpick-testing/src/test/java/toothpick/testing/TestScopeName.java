package toothpick.testing;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import toothpick.Toothpick;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestScopeName {
  @Rule public ToothPickRule toothPickRule = new ToothPickRule(this, "Foo");
  @Rule public ToothPickRule toothPickRuleWithoutScopeName = new ToothPickRule(this);

  @After
  public void tearDown() throws Exception {
    //needs to be performed after test execution
    //not before as rule are initialized before @Before
    Toothpick.reset();
  }

  @Test(expected = IllegalStateException.class)
  public void testSetScopeName_shouldFail_whenScopeNameWasAlreadySet() throws Exception {
    toothPickRule.setScopeName("Bar");
  }

  @Test(expected = IllegalStateException.class)
  public void testSetScopeName_shouldFail_whenScopeNameAlreadyContainsATestModule() throws Exception {
    toothPickRuleWithoutScopeName.setScopeName("Foo");
  }

  @Test
  public void testScopeNameSetByConstruction() throws Exception {
    assertThat(toothPickRule.getScope(), notNullValue());
    assertThat(toothPickRule.getScope().getName(), is((Object) "Foo"));
    assertThat(toothPickRule.getTestModule(), notNullValue());
  }

  @Test
  public void testSetScopeName() throws Exception {
    toothPickRuleWithoutScopeName.setScopeName("Bar");
    assertThat(toothPickRuleWithoutScopeName.getScope(), notNullValue());
    assertThat(toothPickRuleWithoutScopeName.getScope().getName(), is((Object) "Bar"));
    assertThat(toothPickRuleWithoutScopeName.getTestModule(), notNullValue());
  }
}
