package toothpick;

import org.junit.Test;
import toothpick.config.Module;
import toothpick.data.Bar;
import toothpick.data.Foo;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class InjectorImplTest extends ToothPickBaseTest {

  @Test(expected = IllegalArgumentException.class)
  public void toProvider_shoudThrowException_whenBindingIsNull() throws Exception {
    //GIVEN
    Module module = new Module();
    module.getBindingSet().add(null);
    InjectorImpl injector = new InjectorImpl(module);

    //WHEN
    injector.getInstance(null);

    //THEN
    fail("Should not allow null bindings");
  }

  // Modules

  @Test
  public void installOverrideModules_shoudInstallOverrideBindings_whenCalledOnce() {
    //GIVEN
    Foo testFoo = new Foo();
    InjectorImpl injector = new InjectorImpl(new ProdModule());
    injector.installTestModules(new TestModule(testFoo));

    //WHEN
    Foo instance = injector.getInstance(Foo.class);

    //THEN
    assertThat(instance, sameInstance(testFoo));
  }

  @Test
  public void installOverrideModules_shoudNotInstallOverrideBindings_whenCalledWithoutTestModules() {
    //GIVEN
    InjectorImpl injector = new InjectorImpl(new ProdModule());
    injector.installTestModules();

    //WHEN
    Foo instance = injector.getInstance(Foo.class);

    //THEN
    assertThat(instance, notNullValue());
  }

  @Test
  public void installOverrideModules_shoudInstallOverrideBindingsAgain_whenCalledTwice() {
    //GIVEN
    Foo testFoo = new Foo();
    Foo testFoo2 = new Foo();
    InjectorImpl injector = new InjectorImpl(new ProdModule());
    injector.installTestModules(new TestModule(testFoo));
    injector.installTestModules(new TestModule(testFoo2));

    //WHEN
    Foo instance = injector.getInstance(Foo.class);

    //THEN
    assertThat(instance, sameInstance(testFoo2));
  }

  @Test
  public void installOverrideModules_shoudNotOverrideOtherBindings() {
    //GIVEN
    Foo testFoo = new Foo();
    InjectorImpl injector = new InjectorImpl(new ProdModule2());
    injector.installTestModules(new TestModule(testFoo));

    //WHEN
    Foo fooInstance = injector.getInstance(Foo.class);
    Bar barInstance = injector.getInstance(Bar.class);

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
