package toothpick.testing;

import org.easymock.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

class TestMocking {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "Foo");
  @RegisterExtension EasyMockExtension easyMockExtension = new EasyMockExtension(this);

  @Mock Dependency dependency;

  private EntryPoint entryPoint;

  @Test
  void testMock() {
    //GIVEN
    expect(dependency.num()).andReturn(2);
    replay(dependency);

    //WHEN
    entryPoint = toothPickExtension.getInstance(EntryPoint.class);
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
