plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.qrcode"

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
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":androidshared"))
    implementation(project(":strings"))

    implementation(libs.zxingAndroidEmbedded)
    implementation(libs.mlkit.barcodescanning)
    implementation(libs.androidxLifecycleRuntimeKtx)
    implementation(libs.androidMaterial)
    implementation(libs.cameraxView)
    implementation(libs.mlkit.barcodescanning)
    implementation(libs.camera.mlkit.vision)

    val composeBom = platform(libs.androidxComposeBom)
    implementation(composeBom)
    implementation(libs.androidXComposeMaterial)
    implementation(libs.androidXComposeMaterialIconsExtended)
    implementation(libs.androidXConstraintLayoutCompose)

    implementation(libs.androidXComposePreview)
    debugImplementation(libs.androidXComposeTooling)

    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.robolectric)
}
