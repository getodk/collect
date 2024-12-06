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
        unitTests.isIncludeAndroidResources = true
    }
    namespace = "org.odk.collect.audiorecorder"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(project(":async"))
    implementation(project(":androidshared"))
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidxLifecycleLivedataKtx)
    implementation(libs.dagger)
    kapt(libs.daggerCompiler)
    implementation(libs.timber)

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(project(":service-test"))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.androidxTestCoreKtx)
    testImplementation(libs.androidxTestRules)
    testImplementation(libs.androidxArchCoreTesting)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.hamcrest)
}
