plugins {
    id("jetpackcompose.android.library")
    id("jetpackcompose.android.hilt")
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.liam.compose.core.datastore"

    // The protobuf plugin emits its Java + Kotlin output as an AGP *java*-typed
    // generated source dir, which Kotlin/KSP (Hilt) can't resolve. Expose the whole
    // generated tree (java/ + kotlin/) to the Kotlin source set of each variant.
    sourceSets {
        named("debug") {
            kotlin.srcDir("build/generated/java/generateDebugProto")
        }
        named("release") {
            kotlin.srcDir("build/generated/java/generateReleaseProto")
        }
    }
}

// Ensure proto classes are generated before Kotlin compilation / KSP annotation
// processing runs for each main variant (registered lazily so it applies whenever
// the ksp*/compile* tasks are created).
tasks.matching { it.name.matches(Regex("^(ksp|compile)(Debug|Release)Kotlin$")) }
    .configureEach {
        val variant = name.removePrefix("ksp").removePrefix("compile").removeSuffix("Kotlin")
        dependsOn("generate${variant}Proto")
    }

dependencies {
    // UserModel / GatewayAuthModel / IAuthSessionStore (persisted session types + contract) live in
    // :core:model; ITokenProvider is the networking token contract this module fulfils.
    implementation(project(":core:model"))
    implementation(project(":core:networking"))

    // Hilt (hilt + hilt-compiler) comes from the convention plugin.

    // DataStore (preferences + proto)
    implementation(libs.datastore.preferences)
    implementation(libs.datastore)
    implementation(libs.protobuf.kotlin.lite)
}

// Proto DataStore configuration
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin")
            }
        }
    }
}
