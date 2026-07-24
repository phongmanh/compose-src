plugins {
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.compose")
    id("jetpackcompose.android.hilt")
}

android {
    namespace = "com.liam.compose.features.customer"
}

dependencies {
    implementation(project(":core:networking"))
    implementation(project(":core:navigation"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(project(":core:components"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    // Compose (BOM + ui + ui-tooling-preview + debug ui-tooling come from the convention plugin)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material.icons)

    // Hilt (hilt + hilt-compiler) comes from the convention plugin.
    implementation(libs.hilt.navigation)

    // Retrofit service definition + Gson annotations
    implementation(libs.retrofit)
    implementation(libs.gson)
}
