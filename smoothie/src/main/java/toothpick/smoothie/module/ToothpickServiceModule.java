package toothpick.smoothie.module;

import android.app.Service;
import toothpick.config.Module;

public class ToothpickServiceModule extends Module {
  public ToothpickServiceModule(Service service) {
    bind(Service.class).toInstance(service);
  }
}
