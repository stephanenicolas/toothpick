package toothpick.compiler.factory;

import java.util.Arrays;
import javax.annotation.processing.Processor;

final class ProcessorTestUtilities {
  static Iterable<? extends Processor> factoryProcessors() {
    return Arrays.asList(new FactoryProcessor());
  }
}
