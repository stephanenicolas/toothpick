package toothpick.testing;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import toothpick.Toothpick;
import toothpick.config.Module;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

public class TestCustomScopeWithNamedBinding {
  @Rule public ToothPickRule toothPickRule = new ToothPickRule(this, "Foo").setRootRegistryPackage("toothpick.testing");
  EntryPoint entryPoint;

  @After
  public void tearDown() throws Exception {
    //needs to be performed after test execution
    //not before as rule are initialized before @Before
    Toothpick.reset();
  }

  @Test
  public void testGetInstance_shouldReturnNamedBinding_whenAskingNamedBinding() throws Exception {
    //GIVEN
    ModuleWithNamedBindings moduleWithNamedBindings = new ModuleWithNamedBindings();
    toothPickRule.getScope().installModules(moduleWithNamedBindings);

    //WHEN
    entryPoint = toothPickRule.getInstance(EntryPoint.class, "Foo");

    //THEN
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint, sameInstance(moduleWithNamedBindings.instance));
  }

  public static class EntryPoint {
  }

  public static class ModuleWithNamedBindings extends Module {
    public EntryPoint instance = new EntryPoint();

    public ModuleWithNamedBindings() {
      bind(EntryPoint.class).withName("Foo").toInstance(instance);
    }
  }
}
