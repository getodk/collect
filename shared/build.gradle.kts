plugins {
    id("java-library")
    id("kotlin")
}

apply(from = "../config/quality.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(libs.emojiJava)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockitoKotlin)
}

tasks.register("testDebug") {
    dependsOn("test")
}
