plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
}

apply(from = "../config/quality.gradle")

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    buildFeatures {
        viewBinding = true
    }

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
        unitTests.isIncludeAndroidResources = true
    }
    namespace = "org.odk.collect.androidshared"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(project(":shared"))
    implementation(project(":async"))
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxLifecycleLivedataKtx)
    implementation(libs.androidMaterial)
    implementation(libs.androidxFragmentKtx)
    implementation(libs.androidxPreferenceKtx)
    implementation(libs.timber)
    implementation(libs.androidxExinterface)
    implementation(libs.playServicesLocation)

    testImplementation(project(":test-shared"))
    testImplementation(project(":androidtest"))
    testImplementation(libs.junit)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.androidxTestEspressoCore)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.androidxArchCoreTesting)

    androidTestImplementation(libs.androidxTestExtJunit)
    androidTestImplementation(libs.junit)

    debugImplementation(project(":fragments-test"))
}
