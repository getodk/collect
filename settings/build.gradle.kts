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

    packaging {
        resources {
            // These library licenses will be referenced in-app
            excludes += setOf("META-INF/ASL-2.0.txt", "META-INF/LGPL-3.0.txt")

            // Pick first occurrence of any files that cause conflicts
            pickFirst("schema")
        }
    }

    namespace = "org.odk.collect.settings"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":shared"))
    implementation(project(":projects"))

    implementation(libs.jsonSchemaValidator)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockitoKotlin)
}
