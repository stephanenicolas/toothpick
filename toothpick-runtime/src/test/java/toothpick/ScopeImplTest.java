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

  @Test(expected = IllegalArgumentException.class)
  public void toProvider_shoudThrowException_whenBindingIsNull() throws Exception {
    //GIVEN
    Module module = new Module();
    module.getBindingSet().add(null);
    ScopeImpl scope = new ScopeImpl(module);

    //WHEN
    scope.getInstance(null);

    //THEN
    fail("Should not allow null bindings");
  }

  // Modules

  @Test
  public void installOverrideModules_shoudInstallOverrideBindings_whenCalledOnce() {
    //GIVEN
    Foo testFoo = new Foo();
    ScopeImpl scope = new ScopeImpl(new ProdModule());
    scope.installTestModules(new TestModule(testFoo));

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, sameInstance(testFoo));
  }

  @Test
  public void installOverrideModules_shoudNotInstallOverrideBindings_whenCalledWithoutTestModules() {
    //GIVEN
    ScopeImpl scope = new ScopeImpl(new ProdModule());
    scope.installTestModules();

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
  }

  @Test
  public void installOverrideModules_shoudInstallOverrideBindingsAgain_whenCalledTwice() {
    //GIVEN
    Foo testFoo = new Foo();
    Foo testFoo2 = new Foo();
    ScopeImpl scope = new ScopeImpl(new ProdModule());
    scope.installTestModules(new TestModule(testFoo));
    scope.installTestModules(new TestModule(testFoo2));

    //WHEN
    Foo instance = scope.getInstance(Foo.class);

    //THEN
    assertThat(instance, sameInstance(testFoo2));
  }

  @Test
  public void installOverrideModules_shoudNotOverrideOtherBindings() {
    //GIVEN
    Foo testFoo = new Foo();
    ScopeImpl scope = new ScopeImpl(new ProdModule2());
    scope.installTestModules(new TestModule(testFoo));

    //WHEN
    Foo fooInstance = scope.getInstance(Foo.class);
    Bar barInstance = scope.getInstance(Bar.class);

    //THEN
    assertThat(fooInstance, sameInstance(testFoo));
    assertThat(barInstance, notNullValue());
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
}
