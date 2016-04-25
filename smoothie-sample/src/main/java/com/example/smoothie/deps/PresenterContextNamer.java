package com.example.smoothie.deps;

import android.app.Application;
import com.example.smoothie.PersistActivity;
import javax.inject.Inject;

/**
 * As an alternative, we could use any string
 * in the constant PersistActivity.PRESENTER_SCOPE
 * and use {@code @Scoped(PersistActivity.PRESENTER_SCOPE)}
 */
@PersistActivity.Presenter
public class PresenterContextNamer {
  private static int countInstances = 0;

  @Inject Application application;

  public PresenterContextNamer() {
    countInstances++;
  }

  public String getInstanceCount() {
    return "Instance# " + countInstances;
  }
}
