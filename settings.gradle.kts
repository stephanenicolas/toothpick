rootProject.name = "toothpick-compiler-ksp-parent"

include(":toothpick-compiler-ksp")
include(":toothpick-compiler-ksp-core")
include(":toothpick-compiler-ksp-factory")
include(":toothpick-compiler-ksp-memberinjector")
include(":toothpick-compiler-test")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
