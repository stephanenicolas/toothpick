package toothpick;

import javax.inject.Provider;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class LazyImplTest {

  @Test public void testGet_shouldNotCallProviderGetMethod_whenLazyGetIsNotCalled() throws Exception {
    //GIVEN
    Object expected = new Object();
    Provider mockProvider = createMock(Provider.class);
    replay(mockProvider);

    //WHEN
    new LazyImpl(mockProvider);

    //THEN
    verify(mockProvider);
  }

  @Test public void testGet_shouldCallProviderGetMethod_whenLazyGetIsCalled() throws Exception {
    //GIVEN
    Object expected = new Object();
    Provider mockProvider = createMock(Provider.class);
    expect(mockProvider.get()).andReturn(expected);
    replay(mockProvider);
    LazyImpl lazyUnderTest = new LazyImpl(mockProvider);

    //WHEN
    Object actual = lazyUnderTest.get();

    //THEN
    assertThat(actual, sameInstance(expected));
    verify(mockProvider);
  }

  @Test public void testGet_shouldReturnTheSameObject_andCallTheProviderGetOnce_whenCalledMultipleTimes() throws Exception {
    //GIVEN
    Object expected = new Object();
    Provider mockProvider = createMock(Provider.class);
    expect(mockProvider.get()).andReturn(expected).times(1);
    replay(mockProvider);
    LazyImpl lazyUnderTest = new LazyImpl(mockProvider);

    //WHEN
    Object actual = lazyUnderTest.get();
    Object actual2 = lazyUnderTest.get();
    Object actual3 = lazyUnderTest.get();

    //THEN
    assertThat(actual, sameInstance(expected));
    assertThat(actual2, sameInstance(expected));
    assertThat(actual3, sameInstance(expected));
    verify(mockProvider);
  }
}