package toothpick.smoothie.module;

import android.support.v4.app.Fragment;
import toothpick.config.Module;

public class ToothpickSupportFragmentModule extends Module {
  public ToothpickSupportFragmentModule(Fragment fragment) {
    bind(Fragment.class).toInstance(fragment);
  }
}
