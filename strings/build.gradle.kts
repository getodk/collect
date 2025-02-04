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

    namespace = "org.odk.collect.strings"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidxCoreKtx)

    /**
     * We need to expose [AppCompatActivity] for classes in separate modules that
     * extend [LocalizedActivity].
     */
    api(libs.androidxAppcompat)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.robolectric)
}

repositories {
    mavenCentral()
}
