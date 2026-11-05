plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "../config/quality.gradle")

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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

    namespace = "org.odk.collect.testshared"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":async"))
    implementation(project(":audio-clips"))
    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":androidtest"))
    implementation(project(":service-test"))
    implementation(project(":qr-code"))
    implementation(project(":web-page"))
    implementation(libs.androidxRecyclerview)
    implementation(libs.androidxCoreKtx)
    implementation(libs.robolectric)
    implementation(libs.junit)
    implementation(libs.okhttp3Mockwebserver)
    implementation(libs.androidxTestEspressoIntents)
    implementation(libs.androidxTestEspressoContrib)
    implementation(libs.androidMaterial)
    implementation(libs.danlewAndroidJoda)
    implementation(platform(libs.androidxComposeBom))
    implementation(libs.androidXComposeUiTestJunit4)
}
