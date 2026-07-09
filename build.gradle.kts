import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "secrets.gradle")

@Suppress("UNCHECKED_CAST")
fun getSecrets(): java.util.Properties =
    (extra["getSecrets"] as groovy.lang.Closure<*>).call() as java.util.Properties

buildscript {
    repositories {
        google()

        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        classpath(libs.gradle)
        classpath(libs.googleServices)
        classpath(libs.firebaseCrashlyticsGradle)
        classpath(libs.kotlinGradlePlugin)
        classpath(libs.ktlintGradle)
        classpath(libs.gradleVersionsPlugin)
        classpath(libs.navigationSafeArgsGradlePlugin)
        classpath(libs.ossLicensesPlugin)
    }
}

allprojects {
    repositories {
        maven {
            url = uri("$rootDir/.local-m2/")
            metadataSources {
                mavenPom()
                artifact() // Supports artifact only dependencies like those from medicmobile repo
            }
        }

        // Needs to go first to get specialty libraries https://stackoverflow.com/a/48438866/137744
        google()

        mavenLocal() // Only used for javarosa_local dependency
        mavenCentral()

        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://staging.dev.medicmobile.org/_couch/maven-repo")
            metadataSources { artifact() }
        }

        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = getSecrets().getProperty("MAPBOX_DOWNLOADS_TOKEN", "")
            }
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.set(listOf("-Xjvm-default=all"))
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

apply(from = "config/dependency_updates.gradle")

tasks.register<GradleBuild>("checkCode") {
    tasks = listOf("pmd", "ktlintCheck", "checkstyle", "lintDebug")
}

// Create local Maven repo from cached Gradle dependencies
tasks.register<Sync>("cacheToMavenLocal") {
    from(File(gradle.gradleUserHomeDir, "caches/modules-2/files-2.1"))
    into("$rootDir/.local-m2")

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    // Convert from Gradle cache to Maven format
    eachFile {
        val parts = path.split("/")
        path = parts[0].replace(".", "/") +
            "/" + parts[1] +
            "/" + parts[2] +
            "/" + parts[4]
    }

    includeEmptyDirs = false
}

tasks.register<Exec>("testLab") {
    dependsOn("collect_app:assembleDebug", "collect_app:assembleDebugAndroidTest")

    executable = "gcloud"
    args = listOf(
        "beta", "firebase", "test", "android", "run",
        "--type", "instrumentation",
        "--num-uniform-shards=25",
        "--app", "collect_app/build/outputs/apk/debug/ODK-Collect-debug.apk",
        "--test", "collect_app/build/outputs/apk/androidTest/debug/ODK-Collect-debug-androidTest.apk",
        "--device", "model=MediumPhone.arm,version=34,locale=en,orientation=portrait",
        "--timeout", "15m",
        "--directories-to-pull", "/sdcard",
        "--test-targets", "notPackage org.odk.collect.android.regression",
        "--test-targets", "notPackage org.odk.collect.android.benchmark"
    )
}

tasks.register("releaseCheck") {
    dependsOn("testLab", "collect_app:assembleOdkCollectRelease")
}
