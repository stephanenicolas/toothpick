package toothpick.compiler.common

import toothpick.InjectConstructor
import toothpick.ProvidesSingleton
import javax.inject.Inject
import javax.inject.Singleton

data class ToothpickOptions(
    val excludes: Set<String> = setOf("java.*", "android.*"),
    val annotationTypes: Set<String> = setOf(
        Inject::class.qualifiedName!!,
        Singleton::class.qualifiedName!!,
        ProvidesSingleton::class.qualifiedName!!,
        InjectConstructor::class.qualifiedName!!
    ),
    val crashWhenNoFactoryCanBeCreated: Boolean = false,
    val crashWhenInjectedMethodIsNotPackageVisible: Boolean = false,
    val debugLogOriginatingElements: Boolean = false
) {

    companion object {

        /**
         * The name of the annotation processor option to exclude classes from the creation of member
         * scopes & factories. Exclude filters are java regex, multiple entries are comma separated.
         */
        const val Excludes = "toothpick_excludes"

        /**
         * The name of the annotation processor option to let TP know about custom scope annotation
         * classes. This option is needed only in the case where a custom scope annotation is used on a
         * class, and this class doesn't use any annotation processed out of the box by TP (i.e.
         * javax.inject.* annotations). If you use custom scope annotations, it is a good practice to
         * always use this option so that developers can use the new scope annotation in a very free way
         * without having to consider the annotation processing internals.
         */
        const val AnnotationTypes = "toothpick_annotations"

        /**
         * The name of the annotation processor option to make the TP annotation processor crash when it
         * can't generate a factory for a class. By default the behavior is not to crash but emit a
         * warning. Passing the value `true` crashes the build instead.
         */
        const val CrashWhenNoFactoryCanBeCreated = "toothpick_crash_when_no_factory_can_be_created"

        /**
         * The name of the annotation processor option to make the TP annotation processor crash when it
         * detects an annotated method but with a non package-private visibility. By default the behavior
         * is not to crash but emit a warning. Passing the value `true` crashes the build instead.
         */
        const val CrashWhenInjectedMethodIsNotPackageVisible = "toothpick_crash_when_injected_method_is_not_package"

        const val DebugLogOriginatingElements = "toothpick_debug_log_originating_elements"
    }
}

fun Map<String, String>.readOptions(): ToothpickOptions {
    val default = ToothpickOptions()
    return ToothpickOptions(
        excludes = this[ToothpickOptions.Excludes]
            ?.split(',')
            ?.map { it.trim() }
            ?.toSet()
            ?: default.excludes,
        annotationTypes = default.annotationTypes + (
            this[ToothpickOptions.AnnotationTypes]
                ?.split(',')
                ?.map { it.trim() }
                ?: emptyList()
            ),
        crashWhenNoFactoryCanBeCreated = this[ToothpickOptions.CrashWhenNoFactoryCanBeCreated]
            ?.toBoolean()
            ?: default.crashWhenNoFactoryCanBeCreated,
        crashWhenInjectedMethodIsNotPackageVisible = this[ToothpickOptions.CrashWhenInjectedMethodIsNotPackageVisible]
            ?.toBoolean()
            ?: default.crashWhenInjectedMethodIsNotPackageVisible,
        debugLogOriginatingElements = this[ToothpickOptions.DebugLogOriginatingElements]
            ?.toBoolean()
            ?: default.debugLogOriginatingElements
    )
}
