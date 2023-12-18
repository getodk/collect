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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "org.odk.collect.strings"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(Dependencies.androidx_core_ktx)

    /**
     * We need to expose [AppCompatActivity] for classes in separate modules that
     * extend [LocalizedActivity].
     */
    api(Dependencies.androidx_appcompat)

    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.robolectric)
}

repositories {
    mavenCentral()
}
