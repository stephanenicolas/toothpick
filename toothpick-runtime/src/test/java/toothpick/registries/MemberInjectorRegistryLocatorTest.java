package toothpick.registries;

import org.junit.Test;
import toothpick.configuration.Configuration;
import toothpick.MemberInjector;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.data.Bar;
import toothpick.data.Foo;
import toothpick.registries.memberinjector.AbstractMemberInjectorRegistry;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class MemberInjectorRegistryLocatorTest {

  @Test
  public void testGetMemberInjector_shouldReturnNull_whenThereAreNoRegistries() throws Exception {
    //GIVEN
    Toothpick.setConfiguration(Configuration.forProduction().disableReflection());
    MemberInjectorRegistryLocator.setRootRegistry(null);

    //WHEN
    MemberInjector<Foo> memberInjector = MemberInjectorRegistryLocator.getMemberInjector(Foo.class);

    //THEN
    assertThat(memberInjector, nullValue());
  }

  @Test
  public void testGetMemberInjector_shouldFindTheMemberInjector_whenThereIsARegistryThatKnowsTheMemberInjector() throws Exception {
    //GIVEN
    MemberInjectorRegistryLocator.setRootRegistry(new AbstractMemberInjectorRegistry() {
      @Override
      public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
        if (clazz == Foo.class) {
          return (MemberInjector<T>) new FooMemberInjector();
        } else {
          return getMemberInjectorInChildrenRegistries(clazz);
        }
      }
    });

    //WHEN
    MemberInjector<Foo> memberInjector = MemberInjectorRegistryLocator.getMemberInjector(Foo.class);

    //THEN
    assertThat(memberInjector, instanceOf(FooMemberInjector.class));
  }

  @Test
  public void testGetMemberInjector_shouldFindTheMemberInjector_whenAChildRegistryKnowsTheMemberInjector() throws Exception {
    //GIVEN
    AbstractMemberInjectorRegistry rootRegistry = new AbstractMemberInjectorRegistry() {
      @Override
      public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
        return getMemberInjectorInChildrenRegistries(clazz);
      }
    };
    AbstractMemberInjectorRegistry childRegistry = new AbstractMemberInjectorRegistry() {
      @Override
      public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
        if (clazz == Foo.class) {
          return (MemberInjector<T>) new FooMemberInjector();
        } else {
          return getMemberInjectorInChildrenRegistries(clazz);
        }
      }
    };
    rootRegistry.addChildRegistry(childRegistry);
    MemberInjectorRegistryLocator.setRootRegistry(rootRegistry);

    //WHEN
    MemberInjector<Foo> memberInjector = MemberInjectorRegistryLocator.getMemberInjector(Foo.class);

    //THEN
    assertThat(memberInjector, instanceOf(FooMemberInjector.class));
  }

  @Test
  public void testGetMemberInjector_shouldReturnNull_whenThereIsNoRegistryThatKnowsTheMemberInjector() throws Exception {
    //GIVEN
    AbstractMemberInjectorRegistry rootRegistry = new AbstractMemberInjectorRegistry() {
      @Override
      public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
        if (clazz == Foo.class) {
          return (MemberInjector<T>) new FooMemberInjector();
        } else {
          return getMemberInjectorInChildrenRegistries(clazz);
        }
      }
    };
    AbstractMemberInjectorRegistry childRegistry = new AbstractMemberInjectorRegistry() {
      @Override
      public <T> MemberInjector<T> getMemberInjector(Class<T> clazz) {
        if (clazz == Foo.class) {
          return (MemberInjector<T>) new FooMemberInjector();
        } else {
          return getMemberInjectorInChildrenRegistries(clazz);
        }
      }
    };
    rootRegistry.addChildRegistry(childRegistry);
    MemberInjectorRegistryLocator.setRootRegistry(rootRegistry);

    //WHEN
    MemberInjector<Bar> memberInjector = MemberInjectorRegistryLocator.getMemberInjector(Bar.class);

    //THEN
    assertThat(memberInjector, nullValue());
  }

  private static class FooMemberInjector implements MemberInjector<Foo> {
    @Override
    public void inject(Foo foo, Scope scope) {

    }
  }
}