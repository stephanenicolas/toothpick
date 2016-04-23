package toothpick.smoothie.provider;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import javax.inject.Inject;
import javax.inject.Provider;

public class SharedPreferencesProvider implements Provider<SharedPreferences> {
  Application application;

  @Inject
  public SharedPreferencesProvider(Application application) {
    this.application = application;
  }

  @Override
  public SharedPreferences get() {
    return PreferenceManager.getDefaultSharedPreferences(application);
  }
}
