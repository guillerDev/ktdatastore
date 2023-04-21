val gcloudDatastoreVersion: String by project
plugins {
    java
    kotlin("jvm")
    id("maven-publish")
    id("com.google.cloud.artifactregistry.gradle-plugin")
}

group = "com.leakingcode.gclouddatastore"

repositories {
    mavenCentral()
    maven("artifactregistry://europe-west1-maven.pkg.dev/acomult/acomult-jvm")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.leakingcode"
            artifactId = "gclouddatastore"
            from(components["java"])
        }
    }
    repositories {
        maven("artifactregistry://europe-west1-maven.pkg.dev/acomult/acomult-jvm")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":ktdatastore"))
    api("com.google.cloud:google-cloud-datastore:$gcloudDatastoreVersion")
}
