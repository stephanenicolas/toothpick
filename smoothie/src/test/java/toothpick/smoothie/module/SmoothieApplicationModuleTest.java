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
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import toothpick.Scope;
import toothpick.Toothpick;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SmoothieApplicationModuleTest {

  @Test
  public void testModule_shouldReturnApplicationBindings() throws Exception {
    //GIVEN
    Application application = RuntimeEnvironment.application;
    Scope appScope = Toothpick.openScope(application);
    appScope.installModules(new SmoothieApplicationModule(application));

    //WHEN
    Application injectedApp = appScope.getInstance(Application.class);
    AccountManager accountManager = appScope.getInstance(AccountManager.class);
    AssetManager assetManager = appScope.getInstance(AssetManager.class);
    ContentResolver contentResolver = appScope.getInstance(ContentResolver.class);
    Handler handler = appScope.getInstance(Handler.class);
    Resources resources = appScope.getInstance(Resources.class);
    SharedPreferences sharedPreferences = appScope.getInstance(SharedPreferences.class);

    //THEN
    assertThat(injectedApp, is(RuntimeEnvironment.application));
    assertThat(accountManager, notNullValue());
    assertThat(assetManager, notNullValue());
    assertThat(contentResolver, notNullValue());
    assertThat(handler, notNullValue());
    assertThat(resources, notNullValue());
    assertThat(sharedPreferences, notNullValue());
  }

  @Test
  public void testModule_shouldReturnSystemServices() throws Exception {
    //GIVEN
    Application application = RuntimeEnvironment.application;
    Scope appScope = Toothpick.openScope(application);
    appScope.installModules(new SmoothieApplicationModule(application));

    //WHEN
    Application injectedApp = appScope.getInstance(Application.class);
    LocationManager locationManager = appScope.getInstance(LocationManager.class);
    WindowManager windowManager = appScope.getInstance(WindowManager.class);
    ActivityManager activityManager = appScope.getInstance(ActivityManager.class);
    PowerManager powerManager = appScope.getInstance(PowerManager.class);
    AlarmManager alarmManager = appScope.getInstance(AlarmManager.class);
    NotificationManager notificationManager = appScope.getInstance(NotificationManager.class);
    KeyguardManager keyguardManager = appScope.getInstance(KeyguardManager.class);
    Vibrator vibrator = appScope.getInstance(Vibrator.class);
    ConnectivityManager connectivityManager = appScope.getInstance(ConnectivityManager.class);
    //WifiManager wifiManager = appScope.getInstance(WifiManager.class);
    InputMethodManager inputMethodManager = appScope.getInstance(InputMethodManager.class);
    SensorManager sensorManager = appScope.getInstance(SensorManager.class);
    TelephonyManager telephonyManager = appScope.getInstance(TelephonyManager.class);
    AudioManager audioManager = appScope.getInstance(AudioManager.class);
    DownloadManager downloadManager = appScope.getInstance(DownloadManager.class);

    //THEN
    assertThat(injectedApp, is(RuntimeEnvironment.application));
    assertThat(locationManager, notNullValue());
    assertThat(windowManager, notNullValue());
    assertThat(activityManager, notNullValue());
    assertThat(powerManager, notNullValue());
    assertThat(alarmManager, notNullValue());
    assertThat(notificationManager, notNullValue());
    assertThat(keyguardManager, notNullValue());
    assertThat(vibrator, notNullValue());
    assertThat(connectivityManager, notNullValue());
    //assertThat(wifiManager, notNullValue());
    assertThat(inputMethodManager, notNullValue());
    assertThat(sensorManager, notNullValue());
    assertThat(telephonyManager, notNullValue());
    assertThat(audioManager, notNullValue());
    assertThat(downloadManager, notNullValue());
  }

  @Test
  public void testModule_shouldReturnDefaultSharedPreferences() throws Exception {
    //GIVEN
    Application application = RuntimeEnvironment.application;

    String itemKey = "isValid";
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
    sharedPreferences.edit().putBoolean(itemKey, true).commit();

    Scope appScope = Toothpick.openScope(application);
    appScope.installModules(new SmoothieApplicationModule(application));

    //WHEN
    SharedPreferences sharedPreferencesFromScope = appScope.getInstance(SharedPreferences.class);

    //THEN
    assertThat(sharedPreferencesFromScope.getBoolean(itemKey, false), is(true));
  }

  @Test
  public void testModule_shouldReturnNamedSharedPreferences() throws Exception {
    //GIVEN
    Application application = RuntimeEnvironment.application;

    String sharedPreferencesName = "test";
    String itemKey = "isValid";
    SharedPreferences sharedPreferences = application.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
    sharedPreferences.edit().putBoolean(itemKey, true).commit();

    Scope appScope = Toothpick.openScope(application);
    appScope.installModules(new SmoothieApplicationModule(application, sharedPreferencesName));

    //WHEN
    SharedPreferences sharedPreferencesFromScope = appScope.getInstance(SharedPreferences.class);

    //THEN
    assertThat(sharedPreferencesFromScope.getBoolean(itemKey, false), is(true));
  }
}