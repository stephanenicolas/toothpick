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
package toothpick.compiler.memberinjector.targets;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import toothpick.compiler.common.generators.targets.ParamInjectionTarget;

/** Basically all information to create an object / call a constructor of a class. */
public final class MethodInjectionTarget {
  public final List<ParamInjectionTarget> parameters = new ArrayList<>();
  public final TypeElement enclosingClass;
  public final String methodName;
  public final boolean isOverride;
  public final List<TypeElement> exceptionTypes = new ArrayList<>();

  public MethodInjectionTarget(TypeElement enclosingClass, String methodName, boolean isOverride) {
    this.enclosingClass = enclosingClass;
    this.methodName = methodName;
    this.isOverride = isOverride;
  }
}
