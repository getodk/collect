plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    namespace = "org.odk.collect.nbistubs"
}
