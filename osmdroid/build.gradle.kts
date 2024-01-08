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

    namespace = "org.odk.collect.osmdroid"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(project(":maps"))
    implementation(project(":location"))
    implementation(project(":settings"))
    implementation(project(":strings"))

    implementation(Dependencies.osmdroid)
    implementation(Dependencies.androidx_fragment_ktx)
    implementation(Dependencies.androidx_preference_ktx)
    implementation(Dependencies.timber)
    implementation(Dependencies.play_services_location)
    implementation(Dependencies.android_material)
    implementation(Dependencies.dagger)
    kapt(Dependencies.dagger_compiler)
}
