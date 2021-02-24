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
package toothpick.smoothie.provider;

import android.app.Application;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import javax.inject.Provider;

public class PackageManagerProvider implements Provider<PackageManager> {
  Application application;

  public PackageManagerProvider(@NonNull Application application) {
    this.application = application;
  }

  @NonNull
  @Override
  public PackageManager get() {
    return application.getPackageManager();
  }
}
