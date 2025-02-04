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

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    namespace = "org.odk.collect.testshared"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":async"))
    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":androidtest"))
    implementation(project(":service-test"))
    implementation(libs.androidxRecyclerview)
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxWorkRuntime)
    implementation(libs.androidxAppcompat)
    implementation(libs.robolectric)
    implementation(libs.junit)
    implementation(libs.androidxTestEspressoIntents)
    implementation(libs.androidxTestEspressoContrib)
    implementation(libs.androidMaterial)
    implementation(libs.danlewAndroidJoda)
    implementation(libs.androidxFragmentTesting) {
        exclude(group = "androidx.test", module = "monitor") // fixes issue https://github.com/android/android-test/issues/731
    }
}
