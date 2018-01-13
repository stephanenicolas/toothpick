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

  static Iterable<? extends Processor> memberInjectorProcessorsFailingWhenMethodIsNotPackageVisible() {
    final MemberInjectorProcessor memberInjectorProcessor = new MemberInjectorProcessor();
    memberInjectorProcessor.setCrashOrWarnWhenMethodIsNotPackageVisible(true);
    return Arrays.asList(memberInjectorProcessor);
  }

  static Iterable<? extends Processor> memberInjectorProcessors(String toothpickRegistryPackageName,
                                                                List<String> toothpickRegistryChildrenPackageNameList) {
    return memberInjectorProcessors(toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList, "java.*,android.*", false);
  }

  static Iterable<? extends Processor> memberInjectorProcessors(String toothpickRegistryPackageName,
      List<String> toothpickRegistryChildrenPackageNameList, boolean supportObfuscation) {
    return memberInjectorProcessors(toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList, "java.*,android.*", supportObfuscation);
  }

  static Iterable<? extends Processor> memberInjectorProcessors(String toothpickRegistryPackageName,
                                                                List<String> toothpickRegistryChildrenPackageNameList, String toothpickExcludeFilters) {
    return memberInjectorProcessors(toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList,
            toothpickExcludeFilters, false);
  }

  static Iterable<? extends Processor> memberInjectorProcessors(String toothpickRegistryPackageName,
      List<String> toothpickRegistryChildrenPackageNameList, String toothpickExcludeFilters, boolean supportObfuscation) {
    MemberInjectorProcessor memberInjectorProcessor = new MemberInjectorProcessor();
    memberInjectorProcessor.setToothpickRegistryPackageName(toothpickRegistryPackageName);
    memberInjectorProcessor.setToothpickRegistryChildrenPackageNameList(toothpickRegistryChildrenPackageNameList);
    memberInjectorProcessor.setToothpickExcludeFilters(toothpickExcludeFilters);
    memberInjectorProcessor.setSupportObfuscation(supportObfuscation);
    return Arrays.asList(memberInjectorProcessor);
  }
}
