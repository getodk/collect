import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply(from = "../config/quality.gradle")

android {
    compileSdk = Versions.android_compile_sdk

    defaultConfig {
        minSdk = Versions.android_min_sdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    namespace = "org.odk.collect.audioclips"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":async"))
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_core_ktx)
    implementation(Dependencies.androidx_lifecycle_viewmodel_ktx)
    implementation(Dependencies.androidx_lifecycle_livedata_ktx)

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.mockito_kotlin)
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.androidx_arch_core_testing)
    testImplementation(Dependencies.hamcrest)
}
