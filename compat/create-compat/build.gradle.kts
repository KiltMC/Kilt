import net.fabricmc.loom.task.RemapJarTask
import xyz.bluspring.kilt.gradle.loom.KiltLoomPlugin

apply<KiltLoomPlugin>()

base {
    archivesName.set("Kilt-Create-Compat")
}

version = property("mod_version") as String

repositories {
    maven("https://maven.createmod.net/")
}

dependencies {
    modImplementation("maven.modrinth:create-fabric:0.5.1-j-build.1631+mc1.20.1")
    modImplementation("io.github.tropheusj:milk-lib:1.2.60")
    modImplementation("com.tterrag.registrate_fabric:Registrate:1.3.79-MC1.20.1")

    modCompileOnly("dev.engine-room.flywheel:flywheel-fabric-api-1.20.1:1.0.4")
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