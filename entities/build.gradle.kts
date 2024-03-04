import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.entities"

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
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":strings"))
    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":material"))
    implementation(project(":async"))

    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.javarosa) {
        exclude(group = "joda-time")
        exclude(group = "org.hamcrest", module = "hamcrest-all")
    }
    implementation(Dependencies.androidx_appcompat)
    implementation(Dependencies.android_material)
    implementation(Dependencies.androidx_navigation_fragment_ktx)
    implementation(Dependencies.androidx_navigation_ui)
    implementation(Dependencies.dagger)
    kapt(Dependencies.dagger_compiler)

    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.robolectric)
}
