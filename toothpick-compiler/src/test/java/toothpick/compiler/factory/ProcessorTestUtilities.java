package toothpick.compiler.factory;

import toothpick.compiler.memberinjector.MemberInjectorProcessor;

import javax.annotation.processing.Processor;
import java.util.Arrays;

final class ProcessorTestUtilities {
  private ProcessorTestUtilities() {
  }

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
