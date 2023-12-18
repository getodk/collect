import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.metadata"

    compileSdk = Versions.android_compile_sdk

    defaultConfig {
        minSdk = Versions.android_min_sdk

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

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":permissions"))
    implementation(project(":settings"))
    implementation(project(":shared"))

    implementation(Dependencies.javarosa) {
        exclude(group = "joda-time")
        exclude(group = "org.hamcrest", module = "hamcrest-all")
    }
    implementation(Dependencies.timber)

    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.mockito_kotlin)
}
