package toothpick.compiler.common

import javax.annotation.processing.ProcessingEnvironment

data class ToothpickProcessorOptions(
    val excludes: Set<String> = setOf("java.*", "android.*"),
    val annotationTypes: Set<String> = setOf(
        ToothpickProcessor.INJECT_ANNOTATION_CLASS_NAME,
        ToothpickProcessor.SINGLETON_ANNOTATION_CLASS_NAME,
        ToothpickProcessor.PRODUCES_SINGLETON_ANNOTATION_CLASS_NAME,
        ToothpickProcessor.INJECT_CONSTRUCTOR_ANNOTATION_CLASS_NAME
    ),
    val crashWhenNoFactoryCanBeCreated: Boolean = false,
    val crashWhenInjectedMethodIsNotPackageVisible: Boolean = false
)

fun ProcessingEnvironment.readOptions() = ToothpickProcessorOptions().let { default ->
    default.copy(
        excludes = options[ToothpickProcessor.PARAMETER_EXCLUDES]
            ?.split(',')
            ?.map { it.trim() }
            ?.toSet()
            ?: default.excludes,
        annotationTypes = default.annotationTypes + (
            options[ToothpickProcessor.PARAMETER_ANNOTATION_TYPES]
                ?.split(',')
                ?.map { it.trim() }
                ?: emptyList()
            ),
        crashWhenNoFactoryCanBeCreated = options[ToothpickProcessor.PARAMETER_CRASH_WHEN_NO_FACTORY_CAN_BE_CREATED]
            ?.toBoolean()
            ?: default.crashWhenNoFactoryCanBeCreated,
        crashWhenInjectedMethodIsNotPackageVisible = options[ToothpickProcessor.PARAMETER_CRASH_WHEN_INJECTED_METHOD_IS_NOT_PACKAGE]
            ?.toBoolean()
            ?: default.crashWhenInjectedMethodIsNotPackageVisible
    )
}
