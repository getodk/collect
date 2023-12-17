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
        targetSdk = Versions.android_target_sdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    namespace = "org.odk.collect.testshared"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":async"))
    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":androidtest"))
    implementation(project(":servicetest"))
    implementation(Dependencies.androidx_recyclerview)
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_core_ktx)
    implementation(Dependencies.androidx_work_runtime)
    implementation(Dependencies.androidx_appcompat)
    implementation(Dependencies.robolectric)
    implementation(Dependencies.junit)
    implementation(Dependencies.androidx_test_espresso_intents)
    implementation(Dependencies.androidx_test_espresso_contrib)
    implementation(Dependencies.android_material)
    implementation(Dependencies.danlew_android_joda)
    implementation(Dependencies.androidx_fragment_testing) {
        exclude(group = "androidx.test", module = "monitor") // fixes issue https://github.com/android/android-test/issues/731
    }
}
