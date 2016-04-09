package toothpick;

import javax.inject.Provider;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import toothpick.config.Binding;
import toothpick.integration.data.Foo;
import toothpick.integration.data.FooSingleton;
import toothpick.registries.factory.FactoryRegistryLocator;
import toothpick.registries.memberinjector.MemberInjectorRegistryLocator;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MemberInjectorRegistryLocator.class, FactoryRegistryLocator.class })
public class InjectorImplTest extends ToothPickBaseTest {

  @Test
  public void inject_shouldInjectObjectUsingMemberInjector() throws Exception {
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
  public void toProvider_shouldThrowException_whenBindingIsNull() throws Exception {
    //GIVEN
    InjectorImpl injector = new InjectorImpl();

    //WHEN
    injector.toProvider(null);

    //THEN
    fail("Should not allow null bindings");
  }

  @Test
  public void toProvider_shouldReturnAProviderThatUsesTheFactoryToCreateInstances_whenBindingIsSimpleAndClassNotSingleton() throws Exception {
    //GIVEN
    InjectorImpl injector = new InjectorImpl();
    Factory mockFactory = createMock(Factory.class);
    mockStatic(FactoryRegistryLocator.class);
    expect(FactoryRegistryLocator.getFactory(Foo.class)).andReturn(mockFactory).anyTimes();
    expect(mockFactory.hasSingletonAnnotation()).andReturn(false);
    expect(mockFactory.createInstance(injector)).andReturn(new Foo());
    expect(mockFactory.createInstance(injector)).andReturn(new Foo());
    replay(FactoryRegistryLocator.class, mockFactory);

    //WHEN
    Binding<Foo> binding = new Binding<>(Foo.class);
    Provider<Foo> fooProvider = injector.toProvider(binding);
    Foo actualFoo = fooProvider.get();
    fooProvider = injector.getProvider(Foo.class);
    Foo actualFoo2 = fooProvider.get();

    //THEN
    assertThat(actualFoo, notNullValue());
    assertThat(actualFoo2, notNullValue());
    assertThat(actualFoo, not(sameInstance(actualFoo2)));
    verify(FactoryRegistryLocator.class, mockFactory);
  }

  @Test
  public void toProvider_shouldReturnAProviderThatUsesTheFactoryToCreateASingleton_whenBindingIsSimpleAndClassIsSingleton() throws Exception {
    //GIVEN
    InjectorImpl injector = new InjectorImpl();
    Factory mockFactory = createMock(Factory.class);
    mockStatic(FactoryRegistryLocator.class);
    expect(FactoryRegistryLocator.getFactory(FooSingleton.class)).andReturn(mockFactory).anyTimes();
    expect(mockFactory.hasSingletonAnnotation()).andReturn(true);
    expect(mockFactory.createInstance(injector)).andReturn(new FooSingleton());
    expect(mockFactory.createInstance(injector)).andThrow(new RuntimeException("Should not have been called twice"));
    EasyMock.expectLastCall().anyTimes();
    replay(FactoryRegistryLocator.class, mockFactory);

    //WHEN
    Binding<FooSingleton> binding = new Binding<>(FooSingleton.class);
    Provider<FooSingleton> fooProvider = injector.toProvider(binding);
    FooSingleton actualFoo = fooProvider.get();
    //we must ask a provider as there is no contract in TP for provider not to change after they trigger...
    fooProvider = injector.getProvider(FooSingleton.class);
    FooSingleton actualFoo2 = fooProvider.get();

    //THEN
    assertThat(actualFoo, notNullValue());
    assertThat(actualFoo2, notNullValue());
    assertThat(actualFoo, sameInstance(actualFoo2));
    verify(FactoryRegistryLocator.class, mockFactory);
  }

  // Modules
}
