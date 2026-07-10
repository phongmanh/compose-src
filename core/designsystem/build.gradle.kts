plugins {
    // Android baseline + Compose/serialization come from the convention plugins.
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.compose")
}

android {
    namespace = "com.liam.compose.core.designsystem"
}

dependencies {
    // compose ui / ui-tooling-preview / debug ui-tooling + BOM come from the compose convention plugin.

    // Exposed as `api` so consumers (:app, feature modules) that theme their UI with
    // JetpackComposeTheme get the Material 3 types (MaterialTheme, colorScheme, ...) transitively.
    api(libs.androidx.compose.material3)
}
