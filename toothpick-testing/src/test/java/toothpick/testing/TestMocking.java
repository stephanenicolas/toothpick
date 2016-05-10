package toothpick.testing;

import javax.inject.Inject;
import org.easymock.EasyMockRule;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import toothpick.ToothPick;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TestMocking {
  @Rule public ToothPickRule toothPickRule = new ToothPickRule(this, "Foo").setRootRegistryPackage("toothpick.testing");
  @Rule public TestRule chain = RuleChain.outerRule(toothPickRule).around(new EasyMockRule(this));

  EntryPoint entryPoint;
  @Mock Dependency dependency;

  @After
  public void tearDown() throws Exception {
    //needs to be performed after test execution
    //not before as rule are initialized before @Before
    ToothPick.reset();
  }

  @Test
  public void testMock() throws Exception {
    //GIVEN
    expect(dependency.num()).andReturn(2);
    replay(dependency);
    //WHEN
    entryPoint = toothPickRule.getInstance(EntryPoint.class);
    int num = entryPoint.dependency.num();
    //THEN
    verify(dependency);
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint.dependency, notNullValue());
    assertThat(num, is(2));
  }

  public static class EntryPoint {
    @Inject Dependency dependency;
  }

  public static class Dependency {
    public int num() {
      return 1;
    }
  }
}
