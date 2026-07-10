plugins {
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.compose")
    id("jetpackcompose.android.hilt")
}

android {
    namespace = "com.liam.compose.features.settings"
}

dependencies {
    implementation(project(":core:navigation"))
    // UserPreferencesRepository implements ITokenProvider (from :core:networking), so that type
    // must be on the compile classpath even though this module only reads user preferences.
    implementation(project(":core:networking"))
    // Signed-in user (UserModel) and the ChangePassword screen the settings screen navigates to.
    implementation(project(":core:datastore"))
    implementation(project(":feature:auth"))

    // Compose (BOM + ui + ui-tooling-preview + debug ui-tooling come from the convention plugin)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material.icons)

    // Hilt (hilt + hilt-compiler) comes from the convention plugin.
    implementation(libs.hilt.navigation)
}
