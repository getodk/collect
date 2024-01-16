import dependencies.Dependencies

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
    implementation(project(":forms"))
    implementation(project(":shared"))
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.commons_io)
    implementation(Dependencies.junit)
    implementation(Dependencies.mockito_kotlin)
    implementation(Dependencies.hamcrest)
}

tasks.register("testDebug") {
    dependsOn("test")
}
