import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/** The shared `libs` version catalog (declared in build-logic/settings.gradle.kts). */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** Java toolchain baseline every module compiles against (see CLAUDE.md). */
internal val JAVA_VERSION = JavaVersion.VERSION_11

internal const val COMPILE_SDK = 37
internal const val MIN_SDK = 24

/** Common JUnit/Espresso test dependencies applied to every Android module. */
internal fun Project.configureCommonTestDependencies() {
    dependencies {
        add("testImplementation", libs.findLibrary("junit").get())
        add("androidTestImplementation", libs.findLibrary("androidx-espresso-core").get())
        add("androidTestImplementation", libs.findLibrary("androidx-junit").get())
    }
}
