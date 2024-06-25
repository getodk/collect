import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
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

    buildFeatures {
        viewBinding = true
    }

    namespace = "org.odk.collect.maps"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":async"))
    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(project(":material"))
    implementation(project(":settings"))
    implementation(project(":strings"))
    implementation(project(":web-page"))
    implementation(project(":analytics"))
    implementation(project(":lists"))
    implementation(Dependencies.android_material)
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_fragment_ktx)
    implementation(Dependencies.androidx_preference_ktx)
    implementation(Dependencies.timber)

    debugImplementation(project(":fragments-test"))

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.mockito_kotlin)
    testImplementation(Dependencies.androidx_test_espresso_contrib)
    testImplementation(Dependencies.androidx_test_espresso_core)
}
