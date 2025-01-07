plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
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

    namespace = "org.odk.collect.mapbox"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(project(":location"))
    implementation(project(":maps"))
    implementation(project(":settings"))
    implementation(project(":shared"))
    implementation(project(":strings"))
    implementation(project(":async"))
    implementation(libs.playServicesLocation)
    implementation(libs.androidxPreferenceKtx)
    implementation(libs.mapboxAndroidSdk)
    implementation(libs.timber)
    implementation(libs.androidxStartup)

    testImplementation(project(":test-shared"))
    testImplementation(libs.junit)
    testImplementation(libs.mockitoCore)
    testImplementation(libs.hamcrest)
}
