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
    return memberInjectorProcessors(toothpickRegistryPackageName, toothpickRegistryChildrenPackageNameList, "java.*,android.*");
  }

  static Iterable<? extends Processor> memberInjectorProcessors(String toothpickRegistryPackageName,
      List<String> toothpickRegistryChildrenPackageNameList, String toothpickExcludeFilters) {
    MemberInjectorProcessor memberInjectorProcessor = new MemberInjectorProcessor();
    memberInjectorProcessor.setToothpickRegistryPackageName(toothpickRegistryPackageName);
    memberInjectorProcessor.setToothpickRegistryChildrenPackageNameList(toothpickRegistryChildrenPackageNameList);
    memberInjectorProcessor.setToothpickExcludeFilters(toothpickExcludeFilters);
    return Arrays.asList(memberInjectorProcessor);
  }
}
