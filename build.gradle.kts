plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("com.google.cloud.artifactregistry.gradle-plugin") version ("2.2.1")
}

repositories {
    mavenCentral()
}

allprojects {
    version = "0.1.6"
}
