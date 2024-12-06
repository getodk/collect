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
    implementation(project(":forms"))
    implementation(project(":shared"))
    implementation(libs.kotlinStdlib)
    implementation(libs.commonsIo)
    implementation(libs.junit)
    implementation(libs.mockitoKotlin)
    implementation(libs.hamcrest)
}

tasks.register("testDebug") {
    dependsOn("test")
}
