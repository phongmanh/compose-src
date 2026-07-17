plugins {
    // Android baseline + Compose/serialization come from the convention plugins.
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.compose")
}

android {
    namespace = "com.liam.compose.core.components"
}

dependencies {
    // Exposed as `api` so consumers can use Material3 types transitively.
    api(libs.androidx.compose.material3)
}
