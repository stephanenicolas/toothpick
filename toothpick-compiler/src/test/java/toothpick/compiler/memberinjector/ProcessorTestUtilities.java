package toothpick.compiler.memberinjector;

import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Processor;

final class ProcessorTestUtilities {
  private ProcessorTestUtilities() {
  }

  static Iterable<? extends Processor> memberInjectorProcessors() {
    return Arrays.asList(new MemberInjectorProcessor());
  }

  static Iterable<? extends Processor> memberInjectorProcessors(String toothpickRegistryPackageName,
      List<String> toothpickRegistryChildrenPackageNameList) {
    MemberInjectorProcessor factoryProcessor = new MemberInjectorProcessor();
    factoryProcessor.setToothpickRegistryPackageName(toothpickRegistryPackageName);
    factoryProcessor.setToothpickRegistryChildrenPackageNameList(toothpickRegistryChildrenPackageNameList);
    return Arrays.asList(factoryProcessor);
  }
}
