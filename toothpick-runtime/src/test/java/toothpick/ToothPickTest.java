package toothpick;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ToothPickTest extends ToothPickBaseTest {

  @Test
  public void getInjector_shouldNotReturnNull_whenNoInjectorByThisKeyWasCreated() throws Exception {
    //GIVEN

    //WHEN
    Injector injector = ToothPick.openInjector(this);

    //THEN
    assertThat(injector, notNullValue());
  }

  @Test
  public void getInjector_shouldReturnAnInjector_whenThisInjectorByThisKeyWasCreated() throws Exception {
    //GIVEN
    Injector injector = ToothPick.openInjector(this);

    //WHEN
    Injector injector2 = ToothPick.openInjector(this);

    //THEN
    assertThat(injector, notNullValue());
    assertThat(injector, sameInstance(injector2));
  }

  @Test
  public void createInjector_shouldReturnAnInjectorWithAParent_whenThisInjectorByThisKeyWasCreatedWithAParent() throws Exception {
    //GIVEN
    Injector injectorParent = ToothPick.openInjector("foo");

    //WHEN
    Injector injector = ToothPick.openInjector("bar");
    injectorParent.addChild(injector);

    //THEN
    assertThat(injector, notNullValue());
    assertThat(injector.getParentInjector(), sameInstance(injectorParent));
  }

  @Test
  public void reset_shouldClear_WhenSomeInjectorsWereCreated() throws Exception {
    //GIVEN
    Injector injector0 = ToothPick.openInjector("foo");
    Injector injector1 = ToothPick.openInjector("bar");

    //WHEN
    ToothPick.reset();
    Injector injector0AfterReset = ToothPick.openInjector("foo");
    Injector injector1AfterReset = ToothPick.openInjector("bar");

    //THEN
    assertThat(injector0AfterReset, not(sameInstance(injector0)));
    assertThat(injector1AfterReset, not(sameInstance(injector1)));
  }

  @Test
  public void destroyInjector_shouldClearThisInjector_WhenThisInjectorsWasCreated() throws Exception {
    //GIVEN
    Injector injector = ToothPick.openInjector("foo");

    //WHEN
    ToothPick.closeInjector("foo");
    Injector injectorAfterReset = ToothPick.openInjector("foo");

    //THEN
    assertThat(injectorAfterReset, not(sameInstance(injector)));
  }

  @Test
  public void getOrCreateInjector_shouldReturnSameInjector_WhenOneWasCreatedWithSameKey() throws Exception {
    //GIVEN
    Injector injector = ToothPick.openInjector("foo");

    //WHEN
    Injector injector2 = ToothPick.openInjector("foo");

    //THEN
    assertThat(injector, sameInstance(injector2));
  }

  @Test
  public void getOrCreateInjector_shouldReturnANewInjectorInjector_WhenOneWasNotCreatedWithSameKey() throws Exception {
    //GIVEN
    Injector injectorParent = ToothPick.openInjector("bar");

    //WHEN
    Injector injector = ToothPick.openInjector("foo");
    injectorParent.addChild(injector);
    Injector injector2 = ToothPick.openInjector("foo");

    //THEN
    assertThat(injector, notNullValue());
    assertThat(injector2, sameInstance(injector));
    assertThat(injector.getParentInjector(), sameInstance(injectorParent));
  }

  @Test
  public void destroyInjector_shouldNotFail_WhenThisInjectorsWasNotCreated() throws Exception {
    //GIVEN

    //WHEN
    ToothPick.closeInjector("foo");

    //THEN
  }

  @Test
  public void constructor_shouldBePrivate() throws Exception {
    //GIVEN

    //WHEN
    Constructor constructor = ToothPick.class.getDeclaredConstructor();

    //THEN
    assertThat(constructor, notNullValue());
    assertThat(constructor.isAccessible(), is(false));
  }

  @Test(expected = InvocationTargetException.class)
  public void constructor_shouldThrowException_whenCalled() throws Exception {
    //GIVEN

    //WHEN
    Constructor constructor = ToothPick.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();

    //THEN
    fail("default constructor should not be invokable even via reflection");
  }

  @After
  public void tearDown() throws Exception {
    ToothPick.reset();
  }
}