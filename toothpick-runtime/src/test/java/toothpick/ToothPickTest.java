package toothpick;

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

public class ToothPickTest {

  @Test
  public void getInjector_shouldReturnNull_whenNoInjectorByThisKeyWasCreated() throws Exception {
    //GIVEN

    //WHEN
    Injector injector = ToothPick.getInjector(this);

    //THEN
    assertThat(injector, nullValue());
  }

  @Test
  public void getInjector_shouldReturnAnInjector_whenThisInjectorByThisKeyWasCreated() throws Exception {
    //GIVEN
    Injector injector = ToothPick.createInjector(this);

    //WHEN
    Injector injector2 = ToothPick.getInjector(this);

    //THEN
    assertThat(injector, notNullValue());
    assertThat(injector, sameInstance(injector2));
  }

  @Test
  public void createInjector_shouldReturnAnInjectorWithAParent_whenThisInjectorByThisKeyWasCreatedWithAParent() throws Exception {
    //GIVEN
    Injector injectorParent = ToothPick.createInjector("foo");

    //WHEN
    Injector injector = ToothPick.createInjector(injectorParent, "bar");

    //THEN
    assertThat(injector, notNullValue());
    assertThat(injector.getParent(), sameInstance(injectorParent));
  }

  @Test
  public void reset_shouldClear_WhenSomeInjectorsWereCreated() throws Exception {
    //GIVEN
    ToothPick.createInjector("foo");
    ToothPick.createInjector("bar");

    //WHEN
    ToothPick.reset();
    Injector injector0AfterReset = ToothPick.getInjector("foo");
    Injector injector1AfterReset = ToothPick.getInjector("bar");

    //THEN
    assertThat(injector0AfterReset, nullValue());
    assertThat(injector1AfterReset, nullValue());
  }

  @Test
  public void destroyInjector_shouldClearThisInjector_WhenThisInjectorsWasCreated() throws Exception {
    //GIVEN
    ToothPick.createInjector("foo");

    //WHEN
    ToothPick.destroyInjector("foo");
    Injector injectorAfterReset = ToothPick.getInjector("foo");

    //THEN
    assertThat(injectorAfterReset, nullValue());
  }

  @Test
  public void getOrCreateInjector_shouldReturnAnInjector_WhenOneWasCreatedWithSameKey() throws Exception {
    //GIVEN
    Injector injector = ToothPick.createInjector("foo");

    //WHEN
    Injector injector2 = ToothPick.getOrCreateInjector(null, "foo");

    //THEN
    assertThat(injector, sameInstance(injector2));
  }

  @Test
  public void getOrCreateInjector_shouldReturnANewInjectorInjector_WhenOneWasNotCreatedWithSameKey() throws Exception {
    //GIVEN
    Injector injectorParent = ToothPick.createInjector("bar");

    //WHEN
    Injector injector = ToothPick.getOrCreateInjector(injectorParent, "foo");
    Injector injector2 = ToothPick.getOrCreateInjector(injectorParent, "foo");

    //THEN
    assertThat(injector, notNullValue());
    assertThat(injector2, sameInstance(injector));
    assertThat(injector.getParent(), sameInstance(injectorParent));
  }

  @Test
  public void destroyInjector_shouldClearThisInjector_WhenThisInjectorsWasNotCreated() throws Exception {
    //GIVEN

    //WHEN
    ToothPick.destroyInjector("foo");
    Injector injectorAfterReset = ToothPick.getInjector("foo");

    //THEN
    assertThat(injectorAfterReset, nullValue());
  }

  @After
  public void tearDown() throws Exception {
    ToothPick.reset();
  }
}