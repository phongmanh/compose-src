// build-logic is an included (composite) build that supplies this project's
// convention plugins. It has its own settings so it can be pulled in via
// `includeBuild("build-logic")` from the root settings' pluginManagement block.
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Reuse the app's version catalog so convention plugins resolve the same
    // library/plugin versions declared in gradle/libs.versions.toml.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
