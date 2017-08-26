package toothpick.smoothie.provider;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import javax.inject.Provider;

public class SharedPreferencesProvider implements Provider<SharedPreferences> {
  Application application;
  String preferencesName;

  public SharedPreferencesProvider(Application application) {
    this(application, null);
  }

  public SharedPreferencesProvider(Application application, String preferencesName) {
    this.application = application;
    this.preferencesName = preferencesName;
  }

  @Override
  public SharedPreferences get() {
    if (preferencesName != null) {
      return application.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    }
    return PreferenceManager.getDefaultSharedPreferences(application);
  }
}
