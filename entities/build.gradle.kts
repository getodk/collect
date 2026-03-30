plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.safeargsKotlin)
    alias(libs.plugins.composeCompiler)
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.entities"

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
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":strings"))
    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":material"))
    implementation(project(":async"))
    implementation(project(":lists"))
    implementation(project(":forms"))
    implementation(project(":icons"))

    implementation(libs.kotlinStdlib)
    implementation(libs.javarosa) {
        exclude(group = "joda-time")
        exclude(group = "org.hamcrest", module = "hamcrest-all")
    }
    implementation(libs.androidxAppcompat)
    implementation(libs.androidMaterial)
    implementation(libs.androidxNavigationFragmentKtx)
    implementation(libs.androidxNavigationUi)
    implementation(libs.dagger)
    ksp(libs.daggerCompiler)

    val composeBom = platform(libs.androidxComposeBom)
    implementation(composeBom)
    implementation(libs.androidXComposeMaterial)
    implementation(libs.androidXConstraintLayoutCompose)
    implementation(libs.runtime.livedata)
    implementation(libs.androidXComposePreview)
    debugImplementation(libs.androidXComposeTooling)

    testImplementation(project(":forms-test"))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.javarosa) // Include with all dependencies
    testImplementation(libs.androidXComposeUiTestJunit4)
    debugImplementation(libs.androidXComposeUiTestManifest)
}
