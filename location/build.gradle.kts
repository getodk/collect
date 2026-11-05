plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinKsp)
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    namespace = "org.odk.collect.location"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(libs.androidxCoreKtx)
    implementation(libs.playServicesLocation)
    implementation(libs.timber)
    implementation(libs.dagger)
    ksp(libs.daggerCompiler)

    testImplementation(project(":test-shared"))
    testImplementation(project(":service-test"))
    testImplementation(libs.junit)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidxTestExtJunit)
}
