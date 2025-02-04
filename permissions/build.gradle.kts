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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    namespace = "org.odk.collect.permissions"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":strings"))
    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxFragmentKtx)
    implementation(libs.androidMaterial)
    implementation(libs.karumiDexter)
    implementation(libs.timber)

    debugImplementation(project(":fragments-test"))

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(project(":strings"))
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.androidxTestEspressoCore)
    testImplementation(libs.androidxTestEspressoIntents)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.robolectric)
}
