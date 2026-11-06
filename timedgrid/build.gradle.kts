plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.timedgrid"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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

    implementation(project(":androidshared"))
    implementation(project(":strings"))

    implementation(libs.androidMaterial)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidFlexbox)
    implementation(libs.androidxLifecycleViewmodelKtx)
    implementation(libs.javarosa) {
        exclude(group = "joda-time")
        exclude(group = "org.hamcrest", module = "hamcrest-all")
    }
}
