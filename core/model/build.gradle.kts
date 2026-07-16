plugins {
    id("jetpackcompose.android.library")
}

android {
    namespace = "com.liam.compose.core.model"
}

dependencies {
    // Gson annotations on the persisted/wire models (UserModel, GatewayAuthModel).
    implementation(libs.gson)
}
