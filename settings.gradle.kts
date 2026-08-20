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
        maven("https://repo.sleeping.town/")
        maven("https://maven.neoforged.net/releases")
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

includeBuild("build-logic")
includeBuild("twill") {
    dependencySubstitution {
        substitute(module("xyz.bluspring:twill"))
            .using(project(":26.1.2"))
    }
}

//file("compat").listFiles { file -> file.isDirectory && file.name != ".gradle" && file.name != "build" }.forEach {
//    include(":compat:${it.name}")
//    project(":compat:${it.name}").apply {
//        this.projectDir = it
//    }
//}

//include(":loader:quilt") // TODO: Quilt Loom is broken on 1.21.1 from the looks.
