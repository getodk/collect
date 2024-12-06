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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        viewBinding = true
    }
    namespace = "org.odk.collect.geo"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":shared"))
    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(project(":location"))
    implementation(project(":androidshared"))
    implementation(project(":external-app"))
    implementation(project(":async"))
    implementation(project(":analytics"))
    implementation(project(":permissions"))
    implementation(project(":settings"))
    implementation(project(":maps"))
    implementation(project(":material"))
    implementation(project(":web-page"))
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidxLifecycleLivedataKtx)
    implementation(libs.timber)
    implementation(libs.playServicesLocation)
    implementation(libs.androidxFragmentKtx)
    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    debugImplementation(project(":fragments-test"))

    testImplementation(project(":androidtest"))
    testImplementation(project(":settings"))
    testImplementation(project(":test-shared"))
    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidxTestEspressoCore)
    testImplementation(libs.androidxArchCoreTesting)
}
