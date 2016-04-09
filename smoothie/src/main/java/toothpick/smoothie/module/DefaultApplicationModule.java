package toothpick.smoothie.module;

import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import toothpick.config.Module;
import toothpick.smoothie.provider.AccountManagerProvider;
import toothpick.smoothie.provider.AssetManagerProvider;
import toothpick.smoothie.provider.ContentResolverProvider;
import toothpick.smoothie.provider.HandlerProvider;
import toothpick.smoothie.provider.ResourcesProvider;
import toothpick.smoothie.provider.SharedPreferencesProvider;
import toothpick.smoothie.provider.SystemServiceProvider;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class DefaultApplicationModule extends Module {
  public DefaultApplicationModule(Application application) {
    bind(Context.class).to(application);
    bind(Application.class).to(application);
    bind(AccountManager.class).toProvider(AccountManagerProvider.class);
    bind(AssetManager.class).toProvider(AssetManagerProvider.class);
    bind(ContentResolver.class).toProvider(ContentResolverProvider.class);
    bind(Handler.class).toProvider(HandlerProvider.class);
    bind(Resources.class).toProvider(ResourcesProvider.class);
    bind(SharedPreferences.class).toProvider(SharedPreferencesProvider.class);
    bindSystemServices(application);
  }

  // TODO check min sdk and refactor
  private void bindSystemServices(Application application) {
    bind(LocationManager.class).toProvider(new SystemServiceProvider<LocationManager>(application, LOCATION_SERVICE));
    bind(WindowManager.class).toProvider(new SystemServiceProvider<WindowManager>(application, WINDOW_SERVICE));
    bind(ActivityManager.class).toProvider(new SystemServiceProvider<ActivityManager>(application, ACTIVITY_SERVICE));
    bind(PowerManager.class).toProvider(new SystemServiceProvider<PowerManager>(application, POWER_SERVICE));
    bind(AlarmManager.class).toProvider(new SystemServiceProvider<AlarmManager>(application, ALARM_SERVICE));
    bind(NotificationManager.class).toProvider(new SystemServiceProvider<NotificationManager>(application, NOTIFICATION_SERVICE));
    bind(KeyguardManager.class).toProvider(new SystemServiceProvider<KeyguardManager>(application, KEYGUARD_SERVICE));
    bind(Vibrator.class).toProvider(new SystemServiceProvider<Vibrator>(application, VIBRATOR_SERVICE));
    bind(ConnectivityManager.class).toProvider(new SystemServiceProvider<ConnectivityManager>(application, CONNECTIVITY_SERVICE));
    bind(WifiManager.class).toProvider(new SystemServiceProvider<WifiManager>(application, WINDOW_SERVICE));
    bind(InputMethodManager.class).toProvider(new SystemServiceProvider<InputMethodManager>(application, INPUT_METHOD_SERVICE));
    bind(SensorManager.class).toProvider(new SystemServiceProvider<SensorManager>(application, SENSOR_SERVICE));
    bind(TelephonyManager.class).toProvider(new SystemServiceProvider<TelephonyManager>(application, TELEPHONY_SERVICE));
    bind(AudioManager.class).toProvider(new SystemServiceProvider<AudioManager>(application, AUDIO_SERVICE));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      bind(DownloadManager.class).toProvider(new SystemServiceProvider<DownloadManager>(application, DOWNLOAD_SERVICE));
    }
  }
}
