import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Applies `com.android.library` and the shared Android baseline (compileSdk, minSdk,
 * Java 11, instrumentation runner + common test dependencies). Used by every library
 * module: `id("jetpackcompose.android.library")`.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk = COMPILE_SDK
            defaultConfig {
                minSdk = MIN_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
            testOptions {
                unitTests {
                    // android.util.Log throws "not mocked" by default, which would make every
                    // repository and ViewModel that logs untestable on the JVM. Stubbing the
                    // android.jar keeps that business logic unit-testable (Guideline §3).
                    isReturnDefaultValues = true
                }
            }
        }

        configureCommonTestDependencies()
    }
}
