/*
 * Copyright 2019 Stephane Nicolas
 * Copyright 2019 Daniel Molinero Reguera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package toothpick.smoothie.module;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.Context.SEARCH_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import toothpick.config.Module;
import toothpick.smoothie.provider.AccountManagerProvider;
import toothpick.smoothie.provider.AssetManagerProvider;
import toothpick.smoothie.provider.ContentResolverProvider;
import toothpick.smoothie.provider.HandlerProvider;
import toothpick.smoothie.provider.PackageManagerProvider;
import toothpick.smoothie.provider.ResourcesProvider;
import toothpick.smoothie.provider.SharedPreferencesProvider;
import toothpick.smoothie.provider.SystemServiceProvider;

public class SmoothieApplicationModule extends Module {
  public SmoothieApplicationModule(@NonNull Application application) {
    this(application, null);
  }

  public SmoothieApplicationModule(
      @NonNull Application application, @Nullable String preferencesName) {
    bind(Application.class).toInstance(application);
    bind(AccountManager.class).toProviderInstance(new AccountManagerProvider(application));
    bind(AssetManager.class).toProviderInstance(new AssetManagerProvider(application));
    bind(ContentResolver.class).toProviderInstance(new ContentResolverProvider(application));
    bind(Handler.class).toProviderInstance(new HandlerProvider());
    bind(PackageManager.class).toProviderInstance(new PackageManagerProvider(application));
    bind(Resources.class).toProviderInstance(new ResourcesProvider(application));
    bind(SharedPreferences.class)
        .toProviderInstance(new SharedPreferencesProvider(application, preferencesName));
    bindSystemServices(application);
    bindPackageInfo(application);
  }

  private void bindPackageInfo(@NonNull Application application) {
    final PackageInfo packageInfo;
    try {
      packageInfo = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
      bind(PackageInfo.class).toInstance(packageInfo);
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private void bindSystemServices(@NonNull Application application) {
    bindSystemService(application, LocationManager.class, LOCATION_SERVICE);
    bindSystemService(application, WindowManager.class, WINDOW_SERVICE);
    bindSystemService(application, ActivityManager.class, ACTIVITY_SERVICE);
    bindSystemService(application, PowerManager.class, POWER_SERVICE);
    bindSystemService(application, AlarmManager.class, ALARM_SERVICE);
    bindSystemService(application, NotificationManager.class, NOTIFICATION_SERVICE);
    bindSystemService(application, KeyguardManager.class, KEYGUARD_SERVICE);
    bindSystemService(application, Vibrator.class, VIBRATOR_SERVICE);
    bindSystemService(application, ConnectivityManager.class, CONNECTIVITY_SERVICE);
    bindSystemService(application, WifiManager.class, WIFI_SERVICE);
    bindSystemService(application, InputMethodManager.class, INPUT_METHOD_SERVICE);
    bindSystemService(application, SearchManager.class, SEARCH_SERVICE);
    bindSystemService(application, SensorManager.class, SENSOR_SERVICE);
    bindSystemService(application, TelephonyManager.class, TELEPHONY_SERVICE);
    bindSystemService(application, AudioManager.class, AUDIO_SERVICE);
    bindSystemService(application, DownloadManager.class, DOWNLOAD_SERVICE);
    bindSystemService(application, ClipboardManager.class, CLIPBOARD_SERVICE);
  }

  private <T> void bindSystemService(
      Application application, Class<T> serviceClass, String serviceName) {
    bind(serviceClass).toProviderInstance(new SystemServiceProvider<T>(application, serviceName));
  }
}
