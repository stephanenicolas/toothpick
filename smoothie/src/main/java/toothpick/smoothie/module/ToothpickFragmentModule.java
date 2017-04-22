package toothpick.smoothie.module;

import android.app.Fragment;
import toothpick.config.Module;

public class ToothpickFragmentModule extends Module {
  public ToothpickFragmentModule(Fragment fragment) {
    bind(Fragment.class).toInstance(fragment);
  }
}
