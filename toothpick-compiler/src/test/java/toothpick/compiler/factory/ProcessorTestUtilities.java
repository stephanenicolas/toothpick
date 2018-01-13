package toothpick.compiler.factory;

import toothpick.compiler.memberinjector.MemberInjectorProcessor;

import javax.annotation.processing.Processor;
import java.util.Arrays;
import java.util.List;

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

  static Iterable<? extends Processor> factoryProcessors(String toothpickRegistryPackageName, List<String> toothpickRegistryChildrenPackageNameList) {
    return factoryProcessors(toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList, "java.*,android.*", false);
  }

  static Iterable<? extends Processor> factoryProcessors(String toothpickRegistryPackageName,
                                                         List<String> toothpickRegistryChildrenPackageNameList,
                                                         boolean supportObfuscation) {
    return factoryProcessors(toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList, "java.*,android.*", supportObfuscation);
  }

  static Iterable<? extends Processor> factoryProcessors(String toothpickRegistryPackageName,
                                                         List<String> toothpickRegistryChildrenPackageNameList,
                                                         String toothpickExcludeFilters) {
    return factoryProcessors(toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList, toothpickExcludeFilters, false);
  }

  static Iterable<? extends Processor> factoryProcessors(String toothpickRegistryPackageName,
                                                         List<String> toothpickRegistryChildrenPackageNameList,
                                                         String toothpickExcludeFilters,
                                                         boolean supportObfuscation) {
    FactoryProcessor factoryProcessor = new FactoryProcessor();
    factoryProcessor.setToothpickRegistryPackageName(toothpickRegistryPackageName);
    factoryProcessor.setToothpickRegistryChildrenPackageNameList(toothpickRegistryChildrenPackageNameList);
    factoryProcessor.setToothpickExcludeFilters(toothpickExcludeFilters);
    factoryProcessor.setSupportObfuscation(supportObfuscation);
    return Arrays.asList(factoryProcessor);
  }

  static Iterable<? extends Processor> factoryProcessorsWithAdditionalTypes(String... types) {
    FactoryProcessor factoryProcessor = new FactoryProcessor();
    for (String type : types) {
      factoryProcessor.addSupportedAnnotationType(type);
    }

    return Arrays.asList(factoryProcessor);
  }


}
