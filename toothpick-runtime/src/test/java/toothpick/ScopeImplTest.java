package toothpick;

import org.junit.Test;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.Foo;
import toothpick.data.IFoo;
import toothpick.registries.NoFactoryFoundException;

import javax.inject.Provider;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ScopeImplTest extends ToothpickBaseTest {

  @Test
  public void installOverrideModules_shouldInstallOverrideBindings_whenCalledOnce() {
    //GIVEN
    Foo testFoo = new Foo();
    ScopeImpl scope = new ScopeImpl("");
    scope.installTestModules(new TestModule(testFoo));
    scope.installModules(new ProdModule());

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, sameInstance(testFoo));
  }

  @Test
  public void installTestModules_shouldOverrideBindings_whenInstalledAfterProductionModules() {
    //GIVEN
    Foo testFoo = new Foo();
    ScopeImpl scope = new ScopeImpl("");
    scope.installModules(new ProdModule());
    scope.installTestModules(new TestModule(testFoo));

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, sameInstance(testFoo));
  }

  @Test
  public void installOverrideModules_shouldNotInstallOverrideBindings_whenCalledWithoutTestModules() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");
    scope.installTestModules();
    scope.installModules(new ProdModule());

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
  }

  @Test(expected = IllegalStateException.class)
  public void installTestModules_shoudFailToInstallTestsBindingsAgain_whenCalledTwice() {
    //GIVEN
    Foo testFoo = new Foo();
    Foo testFoo2 = new Foo();
    ScopeImpl scope = new ScopeImpl("");
    scope.installTestModules(new TestModule(testFoo));

    //WHEN
    scope.installTestModules(new TestModule(testFoo2));

    //THEN
    fail("Should throw an exception");
  }

  @Test
  public void installOverrideModules_shouldNotOverrideOtherBindings() {
    //GIVEN
    Foo testFoo = new Foo();
    ScopeImpl scope = new ScopeImpl("");
    scope.installTestModules(new TestModule(testFoo));
    scope.installModules(new ProdModule2());

    //WHEN
    Foo fooInstance = scope.getInstance(Foo.class);
    Bar barInstance = scope.getInstance(Bar.class);

    //THEN
    assertThat(fooInstance, sameInstance(testFoo));
    assertThat(barInstance, notNullValue());
  }

  @Test(expected = IllegalStateException.class)
  public void installModules_shouldThrowAnException_whenModuleHasANullBinding() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");

    //WHEN
    scope.installModules(new NullBindingModule());

    //THEN
    fail("Should throw an exception.");
  }

  @Test(expected = IllegalStateException.class)
  public void installModules_shouldFailWhenModuleHasIssues() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");

    //WHEN
    scope.installModules(new BuggyModule());

    //THEN
    fail();
  }

  /* TODO we should have unit tests for this and all this class
  @Test
  public void testLookup() {

  }
  */

  @Test(expected = RuntimeException.class)
  public void testLookup_shouldFail_whenNotFindingABindingForANamedProvider() {
    //GIVEN
    //WHEN
    new ScopeImpl("").lookupProvider(Foo.class, "Bar");

    //THEN
    fail("Should throw an exception");
  }

  @Test(expected = IllegalStateException.class)
  public void testToBinding_shouldFail_whenAddingANullBinding() {
    //GIVEN

    //WHEN
    new ScopeImpl("").toProvider(null);

    //THEN
  }

  @Test(expected = IllegalStateException.class)
  public void getInstance_shouldFail_whenScopeIsClosed() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");

    //WHEN
    scope.close();
    scope.getInstance(Foo.class);

    //THEN
  }

  @Test(expected = IllegalStateException.class)
  public void getLazy_shouldFail_whenScopeIsClosed() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");

    //WHEN
    scope.close();
    scope.getLazy(Foo.class);

    //THEN
  }

  @Test(expected = IllegalStateException.class)
  public void getProvider_shouldFail_whenScopeIsClosed() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");

    //WHEN
    scope.close();
    scope.getProvider(Foo.class);

    //THEN
  }

  @Test(expected = IllegalStateException.class)
  public void lazyGet_shouldFail_whenScopeIsClosed() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");
    Lazy<Foo> lazy = scope.getLazy(Foo.class);

    //WHEN
    scope.close();
    lazy.get();

    //THEN
  }

  @Test(expected = IllegalStateException.class)
  public void providerGet_shouldFail_whenScopeIsClosed() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");
    Provider<Foo> provider = scope.getProvider(Foo.class);

    //WHEN
    scope.close();
    provider.get();

    //THEN
  }

  @Test(expected = IllegalStateException.class)
  public void lazyGet_shouldFail_whenScopeIsClosed_andThereAreNoDependencies() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");
    Lazy<Bar> lazy = scope.getLazy(Bar.class);

    //WHEN
    scope.close();
    lazy.get();

    //THEN
  }

  @Test(expected = IllegalStateException.class)
  public void providerGet_shouldFail_whenScopeIsClosed_andThereAreNoDependencies() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("");
    Provider<Bar> provider = scope.getProvider(Bar.class);

    //WHEN
    scope.close();
    provider.get();

    //THEN
  }

  @Test(expected = NoFactoryFoundException.class)
  public void reset_shouldResetBoundProviders_andFlagTheTestModuleToFalse() throws Exception {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("root");
    scope.installTestModules(new Module() { {
      bind(IFoo.class).to(Foo.class);
    } });

    //WHEN
    scope.reset();

    //THEN
    scope.installTestModules(); // Should not crash
    scope.getInstance(IFoo.class); // Should crash as we don't have the binding for IFoo anymore
  }

  @Test
  public void reset_shouldRebindScope() throws Exception {
    //GIVEN
    ScopeImpl scope = new ScopeImpl("root");

    //WHEN
    scope.reset();

    //THEN
    assertThat(scope.getInstance(Scope.class), notNullValue());
  }

  private static class TestModule extends Module {
    TestModule(Foo foo) {
      bind(Foo.class).toInstance(foo);
    }
  }

  private static class ProdModule extends Module {
    ProdModule() {
      bind(Foo.class).toInstance(new Foo());
    }
  }

  private static class ProdModule2 extends Module {
    ProdModule2() {
      bind(Foo.class).toInstance(new Foo());
      bind(Bar.class).toInstance(new Bar());
    }
  }

  private static class NullBindingModule extends Module {
    NullBindingModule() {
      getBindingSet().add(null);
    }
  }

  private static class BuggyModule extends Module {
    {
      bind(Foo.class).toInstance(null);
    }
  }
}
