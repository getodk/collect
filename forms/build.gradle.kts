plugins {
    alias(libs.plugins.javaLibrary)
    alias(libs.plugins.kotlinLibrary)
}

apply(from = "../config/quality.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.kotlinStdlib)
    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
}
