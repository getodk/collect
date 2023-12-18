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
        getByName("release") {
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

    namespace = "org.odk.collect.material"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":androidshared"))
    implementation(project(":strings"))
    implementation(Dependencies.androidx_appcompat)
    implementation(Dependencies.android_material)
    implementation(Dependencies.androidx_fragment_ktx)
    implementation(Dependencies.kotlin_stdlib)

    debugImplementation(project(":fragmentstest"))

    testImplementation(project(":test-shared"))
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.androidx_test_espresso_core)
    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.mockito_kotlin)
    testImplementation(Dependencies.robolectric)
}
