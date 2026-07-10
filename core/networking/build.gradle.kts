import java.util.Properties

plugins {
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.hilt")
}

// Secrets live in git-ignored secrets.properties (Guideline §9) and are injected via BuildConfig,
// so no key/secret sits in source. Falls back to an env var, then empty for fresh checkouts.
val secretsProperties = Properties().apply {
    val file = rootProject.file("secrets.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun secret(key: String): String = secretsProperties.getProperty(key) ?: System.getenv(key) ?: ""

android {
    namespace = "com.liam.compose.core.networking"

    defaultConfig {
        buildConfigField("String", "GATEWAY_KEY", "\"${secret("GATEWAY_KEY")}\"")
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Hilt (hilt + hilt-compiler) comes from the convention plugin.

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
}
