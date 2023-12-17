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
        targetSdk = Versions.android_target_sdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    namespace = "org.odk.collect.audiorecorder"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(project(":async"))
    implementation(project(":androidshared"))
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_core_ktx)
    implementation(Dependencies.androidx_appcompat)
    implementation(Dependencies.androidx_lifecycle_livedata_ktx)
    implementation(Dependencies.dagger)
    kapt(Dependencies.dagger_compiler)
    implementation(Dependencies.timber)

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(project(":servicetest"))
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.androidx_test_core_ktx)
    testImplementation(Dependencies.androidx_test_rules)
    testImplementation(Dependencies.androidx_arch_core_testing)
    testImplementation(Dependencies.mockito_kotlin)
    testImplementation(Dependencies.hamcrest)
}
