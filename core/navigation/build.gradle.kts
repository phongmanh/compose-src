plugins {
    // Android baseline + Compose/serialization come from the convention plugins.
    // Kotlin itself is provided by AGP 9's built-in Kotlin support.
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.compose")
}

android {
    namespace = "com.liam.compose.core.navigation"
}

dependencies {
    // compose ui / ui-tooling-preview / debug ui-tooling + BOM come from the compose convention plugin.

    // Exposed as `api` so consumers (:app, feature modules) get the Navigation 3
    // types (NavKey, NavBackStack, ...) transitively.
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.lifecycle.viewmodel.navigation3)
    api(libs.kotlinx.serialization.core)
}
