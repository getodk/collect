plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "../config/quality.gradle")

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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

    /*
     * We need to expose [AppCompatActivity] for classes in separate modules that
     * extend [LocalizedActivity].
     */
    api(libs.androidxAppcompat)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.robolectric)
}
