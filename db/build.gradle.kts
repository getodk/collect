import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.db"

    compileSdk = Versions.android_compile_sdk

    defaultConfig {
        minSdk = Versions.android_min_sdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":shared"))
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_core_ktx)
    implementation(Dependencies.timber)

    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.robolectric)
}
