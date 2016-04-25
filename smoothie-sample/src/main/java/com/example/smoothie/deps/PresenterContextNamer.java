package com.example.smoothie.deps;

import android.app.Application;
import com.example.smoothie.PersistActivity;
import javax.inject.Inject;

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
