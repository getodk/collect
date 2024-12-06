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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "org.odk.collect.async"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxCoreKtx)
    implementation(libs.kotlinxCoroutinesAndroid)
    implementation(libs.androidxWorkRuntime)
    implementation(project(":analytics")) {
        exclude("com.google.firebase")
    }

    testImplementation(libs.hamcrest)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.androidxWorkTesting)
    testImplementation(libs.mockitoKotlin)
}
