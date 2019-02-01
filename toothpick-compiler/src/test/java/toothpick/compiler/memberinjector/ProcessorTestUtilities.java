package toothpick.compiler.memberinjector;

import javax.annotation.processing.Processor;
import java.util.Arrays;

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
}
