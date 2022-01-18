package toothpick.compiler.factory

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class FactoryProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment) =
        FactoryProcessor(
            processorOptions = environment.options,
            logger = environment.logger,
            codeGenerator = environment.codeGenerator
        )
}
