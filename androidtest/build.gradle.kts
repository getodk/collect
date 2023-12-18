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
        targetSdk = Versions.android_target_sdk

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
    namespace = "org.odk.collect.androidtest"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(Dependencies.junit)
    implementation(Dependencies.androidx_test_core_ktx)
    implementation(Dependencies.androidx_lifecycle_livedata_ktx)
    implementation(Dependencies.androidx_lifecycle_runtime_ktx)
    implementation(Dependencies.androidx_test_espresso_core)
    implementation(Dependencies.androidx_appcompat)
    implementation(Dependencies.androidx_test_espresso_intents)
    implementation(Dependencies.timber)
}
