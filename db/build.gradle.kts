plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.db"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

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
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":shared"))
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxCoreKtx)
    implementation(libs.timber)

    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.robolectric)
}
