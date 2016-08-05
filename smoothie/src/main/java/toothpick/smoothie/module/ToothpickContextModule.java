package toothpick.smoothie.module;

import android.content.Context;
import toothpick.config.Module;

public class ToothpickContextModule extends Module {
  public ToothpickContextModule(Context context) {
    bind(Context.class).toInstance(context);
  }
}
