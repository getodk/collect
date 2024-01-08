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

    buildFeatures {
        viewBinding = true
    }

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
        unitTests.isIncludeAndroidResources = true
    }
    namespace = "org.odk.collect.androidshared"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(project(":shared"))
    implementation(project(":async"))
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_core_ktx)
    implementation(Dependencies.androidx_lifecycle_livedata_ktx)
    implementation(Dependencies.android_material)
    implementation(Dependencies.androidx_fragment_ktx)
    implementation(Dependencies.androidx_preference_ktx)
    implementation(Dependencies.timber)
    implementation(Dependencies.androidx_exinterface)
    implementation(Dependencies.play_services_location)

    testImplementation(project(":test-shared"))
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.androidx_test_espresso_core)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.mockito_kotlin)
    testImplementation(Dependencies.androidx_arch_core_testing)
    testImplementation(Dependencies.androidx_fragment_testing)

    androidTestImplementation(Dependencies.androidx_test_ext_junit)
    androidTestImplementation(Dependencies.junit)

    debugImplementation(project(":fragmentstest"))
}
