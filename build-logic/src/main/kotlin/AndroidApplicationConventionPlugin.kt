import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Applies `com.android.application` and the shared Android baseline. App-specific
 * identity (applicationId, targetSdk, versionCode/Name, buildTypes) stays in the
 * :app build script. Used via `id("jetpackcompose.android.application")`.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            compileSdk = COMPILE_SDK
            defaultConfig {
                minSdk = MIN_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
        }

        configureCommonTestDependencies()
    }
}
