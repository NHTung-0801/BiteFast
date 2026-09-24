pluginManagement {
    includeBuild("build-logic")
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
    }
}

rootProject.name = "BiteFast"

include(":app")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:common")
include(":core:testing")
include(":feature:auth")
include(":feature:discovery")
include(":feature:detail")
include(":feature:cart")
include(":feature:checkout")
include(":feature:tracking")
include(":feature:order")
include(":feature:profile")
include(":feature:rating")
include(":feature:notification")
