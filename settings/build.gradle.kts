import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

apply(from = "../config/quality.gradle")

android {
    compileSdk = Versions.android_compile_sdk

    defaultConfig {
        minSdk = Versions.android_min_sdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            // These library licenses will be referenced in-app
            excludes += setOf("META-INF/ASL-2.0.txt", "META-INF/LGPL-3.0.txt")

            // Pick first occurrence of any files that cause conflicts
            pickFirst("schema")
        }
    }

    namespace = "org.odk.collect.settings"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":projects"))

    implementation(Dependencies.json_schema_validator)

    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.mockito_kotlin)
}
