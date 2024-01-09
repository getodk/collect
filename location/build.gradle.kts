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
    namespace = "org.odk.collect.location"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(project(":analytics"))
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_core_ktx)
    implementation(Dependencies.play_services_location)
    implementation(Dependencies.timber)
    implementation(Dependencies.androidx_appcompat)

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(project(":servicetest"))
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.mockito_kotlin)
    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.androidx_test_ext_junit)
}
