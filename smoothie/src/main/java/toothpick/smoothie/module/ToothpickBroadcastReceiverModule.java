package toothpick.smoothie.module;

import android.content.BroadcastReceiver;
import toothpick.config.Module;

public class ToothpickBroadcastReceiverModule extends Module {
  public ToothpickBroadcastReceiverModule(BroadcastReceiver broadcastReceiver) {
    bind(BroadcastReceiver.class).toInstance(broadcastReceiver);
  }
}
