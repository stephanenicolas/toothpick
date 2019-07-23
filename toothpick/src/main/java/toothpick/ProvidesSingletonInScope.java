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
package toothpick;

import javax.inject.Singleton;

/**
 * Can only be used to annotate {@link javax.inject.Provider} classes (TODO check this). Indicates
 * that the provider will create a singleton in a scope.
 *
 * <p>The scope must be provided as a second annotation (Scope Annotation). This scope annotation is
 * needed so that the factory will create the provider instance within the right scope.
 *
 * <p>It is different from annotating the provider class with a scope annotation such as {@link
 * Singleton}. In that case, the provider itself would be a singleton, but it tells nothing about
 * the instances provided by the provider.
 *
 * <p>Technically, the provider and the provided instances will all be in the same scope.
 */
public @interface ProvidesSingletonInScope {}
