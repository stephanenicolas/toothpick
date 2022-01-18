package toothpick.compiler.memberinjector

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class MemberInjectorProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment) =
        MemberInjectorProcessor(
            processorOptions = environment.options,
            logger = environment.logger,
            codeGenerator = environment.codeGenerator
        )
}
