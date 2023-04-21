val serializationPropertiesVersion: String by project
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("com.google.cloud.artifactregistry.gradle-plugin")
}

group = "com.leakingcode.ktdatastore"

repositories {
    mavenCentral()
    maven("artifactregistry://europe-west1-maven.pkg.dev/acomult/acomult-jvm")
}

publishing {
    repositories {
        maven("artifactregistry://europe-west1-maven.pkg.dev/acomult/acomult-jvm")
    }
}

kotlin {
    /* Targets configuration omitted.
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */
//    iosArm64() //todo needs xcode in command line
    jvm()
    js(IR) {
        binaries.executable()
        nodejs()
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-properties:$serializationPropertiesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}
