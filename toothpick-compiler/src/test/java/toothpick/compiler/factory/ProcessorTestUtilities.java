package toothpick.compiler.factory;

import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Processor;
import toothpick.compiler.memberinjector.MemberInjectorProcessor;

final class ProcessorTestUtilities {
  private ProcessorTestUtilities() {
  }

  static Iterable<? extends Processor> factoryProcessors() {
    return Arrays.asList(new FactoryProcessor());
  }

  static Iterable<? extends Processor> factoryAndMemberInjectorProcessors() {
    return Arrays.asList(new MemberInjectorProcessor(), new FactoryProcessor());
  }

  static Iterable<? extends Processor> factoryProcessors(String toothpickRegistryPackageName, List<String> toothpickRegistryChildrenPackageNameList) {
    return factoryProcessors(toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList, "java.*,android.*");
  }

  static Iterable<? extends Processor> factoryProcessors(String toothpickRegistryPackageName, List<String> toothpickRegistryChildrenPackageNameList, String toothpickExcludeFilters) {
    FactoryProcessor factoryProcessor = new FactoryProcessor();
    factoryProcessor.setToothpickRegistryPackageName(toothpickRegistryPackageName);
    factoryProcessor.setToothpickRegistryChildrenPackageNameList(toothpickRegistryChildrenPackageNameList);
    factoryProcessor.setToothpickExcludeFilters(toothpickExcludeFilters);
    return Arrays.asList(factoryProcessor);
  }
}
