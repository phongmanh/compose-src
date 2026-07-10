import java.util.Properties

plugins {
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.compose")
    id("jetpackcompose.android.hilt")
}

// Gateway client credentials live in git-ignored secrets.properties (Guideline §9) and are injected
// via BuildConfig, so no secret sits in source. Falls back to an env var, then empty.
val secretsProperties = Properties().apply {
    val file = rootProject.file("secrets.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun secret(key: String): String = secretsProperties.getProperty(key) ?: System.getenv(key) ?: ""

android {
    namespace = "com.liam.compose.features.auth"

    defaultConfig {
        buildConfigField("String", "GATEWAY_CLIENT_ID", "\"${secret("GATEWAY_CLIENT_ID")}\"")
        buildConfigField("String", "GATEWAY_CLIENT_SECRET", "\"${secret("GATEWAY_CLIENT_SECRET")}\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Shared networking infrastructure: BaseRepository, ErrorMapper, AppResponse,
    // and the Retrofit instances (default = main API) provided by NetworkModule.
    implementation(project(":core:networking"))
    implementation(project(":core:navigation"))

    // Compose (BOM + ui + ui-tooling-preview + debug ui-tooling come from the convention plugin)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material.icons)

    // Hilt (hilt + hilt-compiler) comes from the convention plugin.
    implementation(libs.hilt.navigation)

    // Retrofit service definition + Gson annotations
    implementation(libs.retrofit)
    implementation(libs.gson)
}
