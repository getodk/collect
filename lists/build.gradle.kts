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

    buildFeatures {
        viewBinding = true
    }

    namespace = "org.odk.collect.lists"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":androidshared"))
    implementation(project(":material"))
    implementation(project(":strings"))
    implementation(project(":icons"))
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_lifecycle_livedata_ktx)
    implementation(Dependencies.androidx_lifecycle_viewmodel_ktx)
    implementation(Dependencies.androidx_recyclerview)

    testImplementation(project(":test-shared"))
    testImplementation(project(":androidtest"))
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.androidx_test_espresso_core)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.androidx_arch_core_testing)

    debugImplementation(project(":fragments-test"))
}
