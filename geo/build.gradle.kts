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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        viewBinding = true
    }
    namespace = "org.odk.collect.geo"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":shared"))
    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(project(":location"))
    implementation(project(":androidshared"))
    implementation(project(":externalapp"))
    implementation(project(":async"))
    implementation(project(":analytics"))
    implementation(project(":permissions"))
    implementation(project(":maps"))
    implementation(project(":material"))
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_appcompat)
    implementation(Dependencies.androidx_lifecycle_livedata_ktx)
    implementation(Dependencies.android_material)
    implementation(Dependencies.timber)
    implementation(Dependencies.play_services_location)
    implementation(Dependencies.androidx_fragment_ktx)
    implementation(Dependencies.dagger)
    kapt(Dependencies.dagger_compiler)

    debugImplementation(project(":fragmentstest"))
    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.mockito_kotlin)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.androidx_test_espresso_core)
    testImplementation(Dependencies.androidx_arch_core_testing)
    testImplementation(Dependencies.androidx_fragment_testing)
}
