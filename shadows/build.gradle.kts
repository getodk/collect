plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.shadows"
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

    packaging {
        resources {
            // Pick first occurrence of any files that cause conflicts
            pickFirst("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        }
    }
}

dependencies {
    implementation(libs.robolectric)
    implementation(libs.androidxAppcompat)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
}
