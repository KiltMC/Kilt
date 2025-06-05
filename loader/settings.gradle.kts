pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "FabricMC"
        }
        maven("https://maven.quiltmc.org/repository/release/") {
            name = "QuiltMC"
        }
        maven("https://dl.bintray.com/brambolt/public")
        maven("https://mvn.devos.one/releases") {
            name = "devOS Releases"
        }
        maven("https://mvn.devos.one/snapshots") {
            name = "devOS Releases"
        }
        mavenCentral()
        gradlePluginPortal()
    }

    val fabric_kotlin_version: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm") version
                fabric_kotlin_version
                    .split("+kotlin.")[1] // Grabs the sentence after `+kotlin.`
                    .split("+")[0] // Ensures sentences like `+build.1` are ignored
    }
}

include(":cichlid")
include(":fabric")
include(":quilt")