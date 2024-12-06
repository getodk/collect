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

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    namespace = "org.odk.collect.projects"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":material"))
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxFragmentKtx)
    implementation(libs.gson)
    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(project(":test-shared"))
    testImplementation(libs.androidxTestEspressoCore)
    testImplementation(libs.mockitoKotlin)

    debugImplementation(libs.androidxFragmentTesting) {
        exclude(group = "androidx.test", module = "monitor") // fixes issue https://github.com/android/android-test/issues/731
    }
}
