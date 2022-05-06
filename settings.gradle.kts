rootProject.name = "toothpick-ksp"

include(":compiler")
include(":compiler-core")
include(":compiler-factory")
include(":compiler-memberinjector")
include(":compiler-test")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
