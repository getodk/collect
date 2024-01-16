import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.googlemaps"

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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":maps"))
    implementation(project(":settings"))
    implementation(project(":location"))
    implementation(project(":strings"))
    implementation(project(":icons"))

    implementation(Dependencies.androidx_preference_ktx)
    implementation(Dependencies.guava)
    implementation(Dependencies.play_services_maps)
    implementation(Dependencies.play_services_location)
    implementation(Dependencies.timber)
    implementation(Dependencies.android_material)

    implementation(Dependencies.dagger)
    kapt(Dependencies.dagger_compiler)
}
