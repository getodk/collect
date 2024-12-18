plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.safeargsKotlin)
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.entities"

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
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":strings"))
    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":material"))
    implementation(project(":async"))
    implementation(project(":lists"))

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
    kapt(libs.daggerCompiler)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockitoKotlin)
}
