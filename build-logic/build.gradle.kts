plugins {
    `kotlin-dsl`
}

// AGP DSL types (LibraryExtension / ApplicationExtension / CommonExtension) are the
// only compile-time dependency the convention plugins need. Everything else they
// apply — Kotlin Compose, Serialization, KSP, Hilt — is applied by plugin id and is
// provided at runtime by the root project's `apply false` plugin classpath.
dependencies {
    compileOnly(libs.android.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "jetpackcompose.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "jetpackcompose.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "jetpackcompose.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "jetpackcompose.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
    }
}
