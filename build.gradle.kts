plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("com.google.cloud.artifactregistry.gradle-plugin") version ("2.1.5")
}

repositories {
    mavenCentral()
}

allprojects {
    version = "0.1.5"
}
