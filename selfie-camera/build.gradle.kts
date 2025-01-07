plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.selfiecamera"

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
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":androidshared"))
    implementation(project(":strings"))
    implementation(project(":permissions"))
    implementation(project(":external-app"))
    implementation(project(":analytics"))

    implementation(libs.cameraxCore)
    implementation(libs.cameraxView)
    implementation(libs.cameraxLifecycle)
    implementation(libs.cameraxVideo)
    implementation(libs.cameraxCamera2)
    implementation("com.google.guava:guava:33.0.0-android") // Guava is a dependency required by CameraX. It shouldn't be used in any other context and should be removed when no longer necessary.
    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    testImplementation(project(":androidtest"))

    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.robolectric)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidxTestEspressoCore)
}
