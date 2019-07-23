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
package toothpick.compiler.factory;

import java.util.Arrays;
import javax.annotation.processing.Processor;
import toothpick.compiler.memberinjector.MemberInjectorProcessor;

final class ProcessorTestUtilities {
  private ProcessorTestUtilities() {}

  static Iterable<? extends Processor> factoryProcessors() {
    return Arrays.asList(new FactoryProcessor());
  }

  static Iterable<? extends Processor> factoryProcessorsFailingOnNonInjectableClasses() {
    final FactoryProcessor factoryProcessor = new FactoryProcessor();
    factoryProcessor.setCrashWhenNoFactoryCanBeCreated(true);
    return Arrays.asList(factoryProcessor);
  }

  static Iterable<? extends Processor> factoryAndMemberInjectorProcessors() {
    return Arrays.asList(new MemberInjectorProcessor(), new FactoryProcessor());
  }

  static Iterable<? extends Processor> factoryProcessorsWithAdditionalTypes(String... types) {
    FactoryProcessor factoryProcessor = new FactoryProcessor();
    for (String type : types) {
      factoryProcessor.addSupportedAnnotationType(type);
    }

    return Arrays.asList(factoryProcessor);
  }
}
