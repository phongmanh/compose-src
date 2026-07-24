pluginManagement {
    // Composite build supplying the convention plugins (jetpackcompose.android.*).
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "JetpackCompose"
include(":app")
include(":core:navigation")
include(":core:networking")
include(":core:model")
include(":core:datastore")
include(":core:components")
include(":core:designsystem")
include(":feature:auth")
include(":feature:home")
include(":feature:settings")
include(":feature:customer")
