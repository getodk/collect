plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinKsp)
}

apply(from = "../config/quality.gradle")

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    namespace = "org.odk.collect.maplibre"
}

val maplibreDebugSymbols by configurations.creating

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":androidshared"))
    implementation(project(":maps"))
    implementation(project(":settings"))
    implementation(project(":shared"))
    implementation(project(":strings"))
    implementation(libs.androidxFragmentKtx)
    implementation(libs.androidxPreferenceKtx)
    implementation(libs.timber)
    implementation(libs.dagger)
    ksp(libs.daggerCompiler)

    implementation(libs.maplibreAndroidSdk)
    maplibreDebugSymbols(libs.maplibreAndroidSdk)

    implementation(libs.maplibreAnnotationPlugin) {
        exclude(group = "org.maplibre.gl", module = "android-sdk")
    }
    implementation(libs.maplibreScalebarPlugin) {
        exclude(group = "org.maplibre.gl", module = "android-sdk")
    }
}

tasks.register<Zip>("generateNativeDebugSymbols") {
    group = "build"
    description = "Generates MapLibre native debug symbols for Google Play"

    from({
        maplibreDebugSymbols.map { zipTree(it) }
    }) {
        include("prefab/modules/maplibre/libs/android.*/libmaplibre.so")

        eachFile {
            val abi = path
                .substringAfter("prefab/modules/maplibre/libs/android.")
                .substringBefore("/")

            path = "$abi/$name"
        }

        includeEmptyDirs = false
    }

    archiveFileName.set("native-debug-symbols.zip")
    destinationDirectory.set(
        rootProject.layout.projectDirectory
            .dir("collect_app/build/outputs/native-debug-symbols")
    )

    val expectedAbis = setOf(
        "arm64-v8a",
        "armeabi-v7a",
        "x86",
        "x86_64"
    )

    doFirst {
        val symbols = maplibreDebugSymbols
            .flatMap {
                zipTree(it).matching {
                    include("prefab/modules/maplibre/libs/android.*/libmaplibre.so")
                }.files
            }
            .map { it.parentFile.name.removePrefix("android.") }
            .toSet()

        check(symbols == expectedAbis) {
            "Expected MapLibre symbols for $expectedAbis but found $symbols"
        }
    }
}
