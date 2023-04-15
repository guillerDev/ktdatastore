plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("com.google.cloud.artifactregistry.gradle-plugin") version ("2.1.5")
}

repositories {
    mavenCentral()
}

allprojects {
    version = "0.1.0"
}
