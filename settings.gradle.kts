pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "AlgoVizPlus"

include(":app")

include(":core:common")
include(":core:ui")
include(":core:designsystem")
include(":core:network")
include(":core:database")
include(":core:datastore")

include(":data")
include(":domain")

include(":features")
include(":features:auth")
