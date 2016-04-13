package com.example.smoothie.deps;

import android.app.Activity;
import android.app.Application;
import javax.inject.Inject;

public class ContextNamer {

  private static int countInstances = 0;

  @Inject Application application;
  @Inject Activity activity;

  public ContextNamer() {
    countInstances++;
  }

  public String getApplicationName() {
    return application.getClass().getSimpleName();
  }

  public String getActivityName() {
    return activity.getClass().getSimpleName();
  }

  public String getInstanceCount() {
    return "Instance# " + countInstances;
  }
}
