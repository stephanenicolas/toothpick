package com.example.smoothie.deps;

import android.app.Activity;
import android.app.Application;
import javax.inject.Inject;

public class ContextNamer {

  @Inject Application application;
  @Inject Activity activity;

  public String getApplicationName() {
    return application.getClass().getSimpleName();
  }

  public String getActivityName() {
    return activity.getClass().getSimpleName();
  }
}
