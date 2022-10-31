plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.leakingcode"
version = "1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ktdatastore"))
    implementation(project(":gclouddatastore"))

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
