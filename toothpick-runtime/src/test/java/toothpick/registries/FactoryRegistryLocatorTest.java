package toothpick.registries;

import org.junit.Before;
import org.junit.Test;
import toothpick.configuration.Configuration;
import toothpick.Factory;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.data.Bar;
import toothpick.data.Foo;
import toothpick.registries.factory.AbstractFactoryRegistry;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FactoryRegistryLocatorTest {

  @Before
  public void setUp() throws Exception {
    Toothpick.setConfiguration(Configuration.forProduction().disableReflection());
  }

  @Test(expected = RuntimeException.class)
  public void testGetFactory_shouldThrowAnException_whenThereAreNoRegistries() throws Exception {
    //GIVEN
    FactoryRegistryLocator.setRootRegistry(null);

    //WHEN
    FactoryRegistryLocator.getFactory(Foo.class);

    //THEN
    fail("Should throw an exception");
  }

  @Test
  public void testGetFactory_shouldFindTheFactory_whenThereIsARegistryThatKnowsTheFactory() throws Exception {
    //GIVEN
    FactoryRegistryLocator.setRootRegistry(new AbstractFactoryRegistry() {
      @Override
      public <T> Factory<T> getFactory(Class<T> clazz) {
        if (clazz == Foo.class) {
          return (Factory<T>) new FooFactory();
        } else {
          return super.getFactoryInChildrenRegistries(clazz);
        }
      }
    });

    //WHEN
    Factory<Foo> factory = FactoryRegistryLocator.getFactory(Foo.class);

    //THEN
    assertThat(factory, instanceOf(FooFactory.class));
  }

  @Test
  public void testGetFactory_shouldFindTheFactory_whenAChildRegistryKnowsTheFactory() throws Exception {
    //GIVEN
    AbstractFactoryRegistry rootRegistry = new AbstractFactoryRegistry() {
      @Override
      public <T> Factory<T> getFactory(Class<T> clazz) {
        return getFactoryInChildrenRegistries(clazz);
      }
    };
    AbstractFactoryRegistry childRegistry = new AbstractFactoryRegistry() {
      @Override
      public <T> Factory<T> getFactory(Class<T> clazz) {
        if (clazz == Foo.class) {
          return (Factory<T>) new FooFactory();
        } else {
          return null;
        }
      }
    };
    rootRegistry.addChildRegistry(childRegistry);
    FactoryRegistryLocator.setRootRegistry(rootRegistry);

    //WHEN
    Factory<Foo> factory = FactoryRegistryLocator.getFactory(Foo.class);

    //THEN
    assertThat(factory, instanceOf(FooFactory.class));
  }

  @Test(expected = RuntimeException.class)
  public void testGetFactory_shouldThrowAnException_whenThereIsNoRegistryThatKnowsTheFactory() throws Exception {
    //GIVEN
    AbstractFactoryRegistry rootRegistry = new AbstractFactoryRegistry() {
      @Override
      public <T> Factory<T> getFactory(Class<T> clazz) {
        if (clazz == Foo.class) {
          return (Factory<T>) new FooFactory();
        } else {
          return super.getFactoryInChildrenRegistries(clazz);
        }
      }
    };
    AbstractFactoryRegistry childRegistry = new AbstractFactoryRegistry() {
      @Override
      public <T> Factory<T> getFactory(Class<T> clazz) {
        if (clazz == Foo.class) {
          return (Factory<T>) new FooFactory();
        } else {
          return null;
        }
      }
    };
    rootRegistry.addChildRegistry(childRegistry);
    FactoryRegistryLocator.setRootRegistry(rootRegistry);

    //WHEN
    FactoryRegistryLocator.getFactory(Bar.class);

    //THEN
    fail("Should throw an exception");
  }

  private static class FooFactory implements Factory<Foo> {
    @Override
    public Foo createInstance(Scope scope) {
      return null;
    }

    @Override
    public Scope getTargetScope(Scope currentScope) {
      return currentScope;
    }

    @Override
    public boolean hasScopeAnnotation() {
      return false;
    }

    @Override
    public boolean hasProvidesSingletonInScopeAnnotation() {
      return false;
    }
  }
}