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
    namespace = "org.odk.collect.location"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":androidshared"))
    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(project(":analytics"))
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxCoreKtx)
    implementation(libs.playServicesLocation)
    implementation(libs.timber)
    implementation(libs.androidxAppcompat)

    testImplementation(project(":androidtest"))
    testImplementation(project(":test-shared"))
    testImplementation(project(":service-test"))
    testImplementation(libs.junit)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidxTestExtJunit)
}
