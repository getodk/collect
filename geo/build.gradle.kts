plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.composeCompiler)
}

apply(from = "../config/quality.gradle")

android {
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
    }
    namespace = "org.odk.collect.geo"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":shared"))
    implementation(project(":icons"))
    implementation(project(":strings"))
    implementation(project(":location"))
    implementation(project(":androidshared"))
    implementation(project(":external-app"))
    implementation(project(":async"))
    implementation(project(":analytics"))
    implementation(project(":permissions"))
    implementation(project(":settings"))
    implementation(project(":maps"))
    implementation(project(":material"))
    implementation(project(":web-page"))
    implementation(libs.kotlinStdlib)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidxLifecycleLivedataKtx)
    implementation(libs.timber)
    implementation(libs.playServicesLocation)
    implementation(libs.androidxFragmentKtx)
    implementation(libs.dagger)
    ksp(libs.daggerCompiler)
    implementation(libs.javarosa) {
        exclude(group = "joda-time")
        exclude(group = "org.hamcrest", module = "hamcrest-all")
    }

    val composeBom = platform(libs.androidxComposeBom)
    implementation(composeBom)
    implementation(libs.androidXComposeMaterial)
    implementation(libs.androidXComposeMaterialIcons)
    implementation(libs.androidXComposePreview)
    debugImplementation(libs.androidXComposeTooling)

    debugImplementation(project(":fragments-test"))

    testImplementation(project(":androidtest"))
    testImplementation(project(":settings"))
    testImplementation(project(":test-shared"))
    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.androidxTestExtJunit)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidxTestEspressoCore)
    testImplementation(libs.androidxArchCoreTesting)
    testImplementation(libs.androidXComposeUiTestJunit4)
    debugImplementation(libs.androidXComposeUiTestManifest)
}
