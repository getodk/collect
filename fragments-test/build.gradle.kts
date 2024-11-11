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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "org.odk.collect.fragmentstest"
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxTestEspressoCore)
    implementation(libs.androidMaterial)

    //noinspection FragmentGradleConfiguration
    debugApi(libs.androidxFragmentTesting) {
        exclude(group = "androidx.test", module = "monitor") // fixes issue https://github.com/android/android-test/issues/731
    }
}
