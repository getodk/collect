import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
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

    namespace = "org.odk.collect.mapbox"
}

dependencies {
    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(project(":location"))
    implementation(project(":maps"))
    implementation(project(":settings"))
    implementation(project(":shared"))
    implementation(project(":strings"))
    implementation(Dependencies.play_services_location)
    implementation(Dependencies.androidx_preference_ktx)
    implementation(Dependencies.guava)
    implementation(Dependencies.mapbox_android_sdk)
    implementation(Dependencies.timber)
    implementation(Dependencies.androidx_startup)

    testImplementation(project(":test-shared"))
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.mockito_inline)
    testImplementation(Dependencies.hamcrest)
}
