plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinKsp)
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.googlemaps"

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
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":maps"))
    implementation(project(":settings"))
    implementation(project(":location"))
    implementation(project(":strings"))
    implementation(project(":icons"))

    implementation(libs.androidxPreferenceKtx)
    implementation(libs.playServicesMaps)
    implementation(libs.playServicesLocation)
    implementation(libs.timber)
    implementation(libs.androidMaterial)

    implementation(libs.dagger)
    ksp(libs.daggerCompiler)
}
