plugins {
    id("jetpackcompose.android.application")
    id("jetpackcompose.android.compose")
    id("jetpackcompose.android.hilt")
}

android {
    namespace = "com.liam.compose"

    defaultConfig {
        applicationId = "com.liam.compose"
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":core:navigation"))
    implementation(project(":core:networking"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(project(":core:components"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
    implementation(project(":feature:settings"))

    // Compose (BOM + ui + ui-tooling-preview + debug ui-tooling come from the convention plugin)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material.icons)
    implementation(libs.splashscreen)

    // Hilt (hilt + hilt-compiler come from the convention plugin)
    implementation(libs.hilt.navigation)

    // Nav3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // Serialization (Navigation 3 @Serializable nav keys)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.serialization.json)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
