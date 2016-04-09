package toothpick;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import toothpick.config.Module;
import toothpick.integration.data.Bar;
import toothpick.integration.data.Foo;
import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MemberInjectorRegistryLocator.class })
public class InjectorImplTest extends ToothPickBaseTest {

  @Test
  public void inject_shoudInjectObjectUsingMemberInjector() throws Exception {
    //GIVEN
    Foo foo = new Foo();
    InjectorImpl injector = new InjectorImpl();
    MemberInjector mockMemberInjector = createMock(MemberInjector.class);
    mockStatic(MemberInjectorRegistryLocator.class);
    expect(MemberInjectorRegistryLocator.getMemberInjector(Foo.class)).andReturn(mockMemberInjector);
    mockMemberInjector.inject(foo, injector);
    expectLastCall();
    replay(MemberInjectorRegistryLocator.class);
    replay(mockMemberInjector);

    //WHEN
    injector.inject(foo);

    //THEN
    verify(MemberInjectorRegistryLocator.class);
    verify(mockMemberInjector);
  }

  @Test(expected = IllegalStateException.class)
  public void toProvider_shoudThrowException_whenBindingIsNull() throws Exception {
    //GIVEN
    InjectorImpl injector = new InjectorImpl();

    //WHEN
    injector.toProvider(null);

    //THEN
    fail("Should not allow null bindings");
  }

  // Modules

  @Test
  public void installOverrideModules_shoudInstallOverrideBindings_whenCalledOnce() {
    //GIVEN
    Foo testFoo = new Foo();
    InjectorImpl injector = new InjectorImpl(new ProdModule());
    injector.installOverrideModules(new TestModule(testFoo));

    //WHEN
    Foo instance = injector.getInstance(Foo.class);

    //THEN
    assertThat(instance, sameInstance(testFoo));
  }

  @Test
  public void installOverrideModules_shoudNotInstallOverrideBindings_whenCalledWithoutTestModules() {
    //GIVEN
    InjectorImpl injector = new InjectorImpl(new ProdModule());
    injector.installOverrideModules();

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
    injector.installOverrideModules(new TestModule(testFoo));
    injector.installOverrideModules(new TestModule(testFoo2));

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
    injector.installOverrideModules(new TestModule(testFoo));

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
