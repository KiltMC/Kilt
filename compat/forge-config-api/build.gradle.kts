import net.fabricmc.loom.task.RemapJarTask
import org.gradle.kotlin.dsl.dependencies

base {
    archivesName.set("Kilt-NeoForge-Config-API")
}

version = property("mod_version") as String

dependencies {
    implementation("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${property("forgeconfigapiport_version")}")
}

tasks {
    processResources {
        val properties = mutableMapOf(
            "version" to project.version,
            "loader_version" to project.property("loader_version"),
            "fabric_version" to project.property("fabric_version"),
            "minecraft_version" to project.property("minecraft_version"),
            "fabric_kotlin_version" to project.property("fabric_kotlin_version")
        )

        for ((key, value) in properties) {
            inputs.property(key, value)
        }

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }
}

// Add compat layer to nested JARs in base Kilt project.
rootProject.tasks.getByName<RemapJarTask>("remapJar").nestedJars.from(project.tasks.getByName("remapJar"))
