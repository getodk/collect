plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinKsp)
}

apply(from = "../config/quality.gradle")

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "org.odk.collect.imageloader"
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.glide)
    implementation(libs.caverockAndroidsvg)
    ksp(libs.glideKsp)
}
