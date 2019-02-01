package toothpick.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import toothpick.config.Module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsSame.sameInstance;

class TestCustomScopeWithNamedBinding {

  @RegisterExtension ToothPickExtension toothPickExtension = new ToothPickExtension(this, "Foo");

  private EntryPoint entryPoint;

  @Test
  void testGetInstance_shouldReturnNamedBinding_whenAskingNamedBinding() throws Exception {
    //GIVEN
    ModuleWithNamedBindings moduleWithNamedBindings = new ModuleWithNamedBindings();
    toothPickExtension.getScope().installModules(moduleWithNamedBindings);

    //WHEN
    entryPoint = toothPickExtension.getInstance(EntryPoint.class, "Foo");

    //THEN
    assertThat(entryPoint, notNullValue());
    assertThat(entryPoint, sameInstance(moduleWithNamedBindings.instance));
  }

  private static class EntryPoint {
  }

  private static class ModuleWithNamedBindings extends Module {
    EntryPoint instance = new EntryPoint();

    ModuleWithNamedBindings() {
      bind(EntryPoint.class).withName("Foo").toInstance(instance);
    }
  }
}
