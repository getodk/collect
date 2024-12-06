plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

apply(from = "../config/quality.gradle")

android {
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

    buildFeatures {
        viewBinding = true
    }

    namespace = "org.odk.collect.errors"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":strings"))
    implementation(project(":androidshared"))
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidxRecyclerview)
    implementation(libs.androidMaterial)

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.androidxTestEspressoCore)
}
