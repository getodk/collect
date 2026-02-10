plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.metadata"

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
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":permissions"))
    implementation(project(":settings"))
    implementation(project(":shared"))

    implementation(libs.javarosa) {
        exclude(group = "joda-time")
        exclude(group = "org.hamcrest", module = "hamcrest-all")
    }
    implementation(libs.timber)

    testImplementation(libs.hamcrest)
    testImplementation(libs.junit)
    testImplementation(libs.mockitoKotlin)
}
