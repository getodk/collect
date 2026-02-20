plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinKsp)
}

apply(from = "../config/quality.gradle")

android {
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "org.odk.collect.osmdroid"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(project(":maps"))
    implementation(project(":location"))
    implementation(project(":settings"))
    implementation(project(":strings"))

    implementation(libs.osmdroid)
    implementation(libs.androidxFragmentKtx)
    implementation(libs.androidxPreferenceKtx)
    implementation(libs.timber)
    implementation(libs.playServicesLocation)
    implementation(libs.androidMaterial)
    implementation(libs.dagger)
    ksp(libs.daggerCompiler)
}
