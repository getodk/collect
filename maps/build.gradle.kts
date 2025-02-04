plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinParcelize)
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

    namespace = "org.odk.collect.maps"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":async"))
    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(project(":material"))
    implementation(project(":settings"))
    implementation(project(":strings"))
    implementation(project(":web-page"))
    implementation(project(":analytics"))
    implementation(project(":lists"))
    implementation(libs.androidMaterial)
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxFragmentKtx)
    implementation(libs.androidxPreferenceKtx)
    implementation(libs.timber)

    debugImplementation(project(":fragments-test"))

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(libs.junit)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.androidxTestEspressoContrib)
    testImplementation(libs.androidxTestEspressoCore)
}
