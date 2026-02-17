plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        viewBinding = true
    }

    namespace = "org.odk.collect.material"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":androidshared"))
    implementation(project(":strings"))
    implementation(project(":icons"))
    implementation(libs.androidxAppcompat)
    api(libs.androidMaterial)
    implementation(libs.androidxFragmentKtx)
    implementation(libs.kotlinStdlib)

    val composeBom = platform(libs.androidxComposeBom)
    implementation(composeBom)
    implementation(libs.androidXComposeMaterial)
    implementation(libs.androidXComposePreview)
    debugImplementation(libs.androidXComposeTooling)

    debugImplementation(project(":fragments-test"))

    testImplementation(project(":test-shared"))
    testImplementation(libs.junit)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.androidxTestEspressoCore)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.robolectric)
}
