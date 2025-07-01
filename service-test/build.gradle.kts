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

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources {
            // Pick first occurrence of any files that cause conflicts
            pickFirst("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        }
    }

    namespace = "org.odk.collect.servicetest"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.androidxLifecycleRuntimeKtx)
    implementation(libs.robolectric)
}
