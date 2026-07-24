plugins {
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.compose")
    id("jetpackcompose.android.hilt")
}

android {
    namespace = "com.liam.compose.features.home"
}

dependencies {
    implementation(project(":core:networking"))
    implementation(project(":core:navigation"))
    // Signed-in user (fullName for the greeting) is read from the proto DataStore;
    // :core:model exposes the UserModel type the repository returns.
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))
    // Shared skeleton/shimmer placeholders used by the loading state.
    implementation(project(":core:components"))
    // Customers tile navigates via feature:customer's CustomerKey.
    implementation(project(":feature:customer"))

    implementation(libs.androidx.appcompat)
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
}
