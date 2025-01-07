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

    buildFeatures {
        viewBinding = true
    }

    namespace = "org.odk.collect.lists"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":androidshared"))
    implementation(project(":material"))
    implementation(project(":strings"))
    implementation(project(":icons"))
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxLifecycleLivedataKtx)
    implementation(libs.androidxLifecycleViewmodelKtx)
    implementation(libs.androidxRecyclerview)

    testImplementation(project(":test-shared"))
    testImplementation(project(":androidtest"))
    testImplementation(libs.junit)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.androidxTestEspressoCore)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidxArchCoreTesting)

    debugImplementation(project(":fragments-test"))
}
