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
    namespace = "org.odk.collect.permissions"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":strings"))
    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(Dependencies.androidx_core_ktx)
    implementation(Dependencies.androidx_fragment_ktx)
    implementation(Dependencies.android_material)
    implementation(Dependencies.karumi_dexter)
    implementation(Dependencies.timber)

    debugImplementation(project(":fragmentstest"))

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(project(":strings"))
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.androidx_test_espresso_core)
    testImplementation(Dependencies.androidx_test_espresso_intents)
    testImplementation(Dependencies.mockito_kotlin)
    testImplementation(Dependencies.robolectric)
}
