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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    namespace = "org.odk.collect.externalapp"
}

dependencies {
    implementation(libs.androidxCoreKtx)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockitoKotlin)
}
