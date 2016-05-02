package toothpick;

import org.junit.Test;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.Foo;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ScopeImplTest extends ToothPickBaseTest {

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
  public void installModule_shouldThrowAnException_whenModuleHasANullBinding() {
    //GIVEN
    Foo testFoo = new Foo();
    ScopeImpl scope = new ScopeImpl("");

    //WHEN
    scope.installModules(new NullBindingModule());

    //THEN
    fail("Should throw an exception.");
  }

  private static class TestModule extends Module {
    public TestModule(Foo foo) {
      bind(Foo.class).to(foo);
    }
  }

  private static class ProdModule extends Module {
    public ProdModule() {
      bind(Foo.class).to(new Foo());
    }
  }

  private static class ProdModule2 extends Module {
    public ProdModule2() {
      bind(Foo.class).to(new Foo());
      bind(Bar.class).to(new Bar());
    }
  }

  private static class NullBindingModule extends Module {
    public NullBindingModule() {
      getBindingSet().add(null);
    }
  }
}
