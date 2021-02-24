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

import android.app.Activity;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import toothpick.config.Module;
import toothpick.smoothie.provider.FragmentManagerProvider;
import toothpick.smoothie.provider.LayoutInflaterProvider;
import toothpick.smoothie.provider.LoaderManagerProvider;

@SuppressWarnings("deprecation")
public class SmoothieActivityModule extends Module {
  public SmoothieActivityModule(@NonNull Activity activity) {
    bind(Activity.class).toInstance(activity);
    bind(android.app.FragmentManager.class)
        .toProviderInstance(new FragmentManagerProvider(activity));
    bind(android.app.LoaderManager.class).toProviderInstance(new LoaderManagerProvider(activity));
    bind(LayoutInflater.class).toProviderInstance(new LayoutInflaterProvider(activity));
  }
}
